/*
 * Copyright (C) 2008-2018 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.workflow.mirror;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Label;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.WorkflowMirrorModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.orgstruct.OrgStructService;
import ru.citeck.ecos.spring.registry.MappingRegistry;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.utils.TransactionUtils;

import java.io.Serializable;
import java.util.*;

public class WorkflowMirrorServiceImpl extends BaseProcessorExtension implements WorkflowMirrorService {

    private static final String QUERY_TASK_BY_ID = "TYPE:\"bpm:task\" AND =cm\\:name:\"%s\"";
    private static final String QUERY_TASKS_BY_WORKFLOW_ID = "TYPE:\"bpm:task\" AND =wfm\\:workflowId:\"%s\"";
    private static final String KEY_TASKS_TO_MIRROR = WorkflowMirrorServiceImpl.class.getName() + ".tasks_to_mirror";

    private static Log logger = LogFactory.getLog(WorkflowMirrorServiceImpl.class);

    private NodeService nodeService;
    private ActionService actionService;
    private PersonService personService;
    private AuthorityService authorityService;
    private WorkflowService workflowService;
    private NodeInfoFactory nodeInfoFactory;
    private OrgStructService orgStructService;
    private SearchService searchService;
    private DictionaryService dictionaryService;
    private MessageLookup messageLookup;
    private NodeService mlAwareNodeService;
    private NamespaceService namespaceService;
    private MappingRegistry documentToCounterparty;

    private NodeUtils nodeUtils;

    private NodeRef taskMirrorRoot;
    private QName taskMirrorAssoc;

    @Override
    public void mirrorTask(String taskId) {
        mirrorTaskBeforeCommit(taskId);
    }

    @Override
    public void mirrorTask(WorkflowTask task) {
        mirrorTaskBeforeCommit(task.getId());
    }

    @Override
    public void mirrorTask(NodeRef taskMirror) {
        if (!nodeService.exists(taskMirror)) {
            return;
        }
        mirrorTaskBeforeCommit((String) nodeService.getProperty(taskMirror, ContentModel.PROP_NAME));
    }

    @Override
    public void mirrorAllTasks() {
        List<WorkflowTask> allTasks = getAllTasks();
        for (WorkflowTask task : allTasks) {
            mirrorTask(task);
        }
    }

    @Override
    public void mirrorTaskAsync(String taskId) {
        Action action = actionService.createAction(MirrorActionExecuter.NAME);
        action.setParameterValue(MirrorActionExecuter.PARAM_TASK_ID, taskId);
        actionService.executeAction(action, null, false, true);
    }

    @Override
    public void mirrorAllTasksAsync() {
        List<WorkflowTask> allTasks = getAllTasks();
        for (WorkflowTask task : allTasks) {
            mirrorTaskAsync(task.getId());
        }
    }

    @Override
    public NodeRef getTaskMirror(String taskId) {
        String query = String.format(QUERY_TASK_BY_ID, taskId);
        List<NodeRef> tasksList = searchQuery(query);
        return !tasksList.isEmpty() ? tasksList.get(0) : null;
    }

    @Override
    public List<NodeRef> getTaskMirrorsByWorkflowId(String workflowId) {
        String query = String.format(QUERY_TASKS_BY_WORKFLOW_ID, workflowId);
        return searchQuery(query);
    }

    private List<NodeRef> searchQuery(String query) {

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
        searchParameters.setQuery(query);

        ResultSet resultSet;
        try {
            resultSet = searchService.query(searchParameters);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Nodes search failed. Query: '" + query + "'", e);
        }
        try {
            List<NodeRef> nodeRefs = resultSet.getNodeRefs();
            if (nodeRefs == null) {
                nodeRefs = Collections.emptyList();
            }
            return nodeRefs;
        } finally {
            resultSet.close();
        }
    }

    private WorkflowTask getTask(String taskId) {
        return workflowService.getTaskById(taskId);
    }

    private void mirrorTaskBeforeCommit(String taskId) {
        TransactionUtils.processAfterBehaviours(KEY_TASKS_TO_MIRROR, taskId, id ->
                mirrorTaskImpl(getTask(id), getTaskMirror(id))
        );
    }

    private void mirrorTaskImpl(WorkflowTask task, NodeRef taskMirror) {

        NodeInfo nodeInfo = null;
        if (task != null) {
            nodeInfo = nodeInfoFactory.createNodeInfo(task);
        }

        // create
        if (task != null && taskMirror == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating mirror for task " + task.getId());
            }

            taskMirror = createTaskMirror(task.getId(), nodeInfo.getType());
        }

        // update
        if (task != null && taskMirror != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating mirror for task " + task.getId() + " (" + taskMirror + ")");
            }

            // override cm:name to allow search of task-mirror by name
            nodeInfo.setProperty(ContentModel.PROP_NAME, task.getId());

            // add convenient attributes, specific to task-mirrors only
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_TASK_TYPE, nodeInfo.getType());
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_WORKFLOW_ID, task.getPath().getInstance().getId());
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_ACTORS, getActors(task));
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_ASSIGNEE, getAssignee(task));
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_ASSIGNEE_MANAGER, getAssigneeManager(task));
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_WORKFLOW_NAME, getWorkflowName(task, nodeInfo));
            nodeInfo.setProperty(WorkflowMirrorModel.PROP_WORKFLOW_INITIATOR, getWorkflowInitiator(task, nodeInfo));

            NodeRef document = getDocument(task, nodeInfo);
            if (document != null) {
                nodeInfo.setProperty(WorkflowMirrorModel.PROP_DOCUMENT, document);
                nodeInfo.setProperty(WorkflowMirrorModel.PROP_DOCUMENT_TYPE, nodeService.getType(document));
                nodeInfo.setProperty(WorkflowMirrorModel.PROP_DOCUMENT_TYPE_TITLE, getMlDocumentTypeTitle(document));
                nodeInfo.setProperty(WorkflowMirrorModel.PROP_COUNTERPARTY, getCounterparty(document));

                NodeRef documentKind = getDocumentKind(document);
                if (documentKind != null && nodeService.exists(documentKind)) {
                    nodeInfo.setProperty(WorkflowMirrorModel.PROP_DOCUMENT_KIND, documentKind);
                    nodeInfo.setProperty(WorkflowMirrorModel.PROP_DOCUMENT_KIND_TITLE,
                            mlAwareNodeService.getProperty(documentKind, ContentModel.PROP_TITLE));
                }

                nodeInfo.createSourceAssociation(document, WorkflowMirrorModel.ASSOC_MIRROR_TASK);
            }

            if (nodeUtils.isValidNode(taskMirror)) {
                nodeInfoFactory.persist(taskMirror, nodeInfo, true);
            }

        } else if (task == null && taskMirror != null) {
            if (logger.isDebugEnabled()) {
                String taskId = (String) nodeService.getProperty(taskMirror, ContentModel.PROP_NAME);
                logger.debug("Deleting mirror for task " + taskId + " (" + taskMirror + ")");
            }
            if (nodeUtils.isValidNode(taskMirror)) {
                nodeService.deleteNode(taskMirror);
            }
        }
    }

    private NodeRef createTaskMirror(String taskId, QName taskType) {
        QName assocQName = QName.createQName(taskMirrorAssoc.getNamespaceURI(), taskId);
        ChildAssociationRef mirrorRef = nodeService.createNode(taskMirrorRoot, taskMirrorAssoc, assocQName, taskType);
        return mirrorRef.getChildRef();
    }

    private LinkedList<NodeRef> getActors(WorkflowTask task) {
        String assigneeName = (String) task.getProperties().get(ContentModel.PROP_OWNER);
        LinkedList<NodeRef> results = new LinkedList<NodeRef>();
        if (assigneeName == null) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            List<NodeRef> pooledActors = (List) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
            String originalOwner = (String) task.getProperties().get(QName.createQName("", "taskOriginalOwner"));
            NodeRef originalOwnerNodeRef = null;
            if (originalOwner != null) {
                originalOwnerNodeRef = authorityService.getAuthorityNodeRef(originalOwner);
            }
            if (pooledActors != null) {
                if (originalOwnerNodeRef != null) {
                    if (pooledActors.contains(originalOwnerNodeRef) && pooledActors.indexOf(originalOwnerNodeRef) != -1) {
                        pooledActors.remove(pooledActors.indexOf(originalOwnerNodeRef));
                        pooledActors.add(0, originalOwnerNodeRef);
                    }
                }

                results.addAll(pooledActors);
            }
        } else {
            NodeRef assignee = personService.getPerson(assigneeName);
            results.add(assignee);
        }
        return results;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private List<WorkflowTask> getAllTasks() {
        WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
        taskQuery.setActive(null);
        taskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        List<WorkflowTask> completedTasks = workflowService.queryTasks(taskQuery);
        taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
        List<WorkflowTask> uncompletedTasks = workflowService.queryTasks(taskQuery);
        return ListUtils.union(completedTasks, uncompletedTasks);
    }

    private NodeRef getAssignee(WorkflowTask task) {
        String assigneeName = (String) task.getProperties().get(ContentModel.PROP_OWNER);
        if (assigneeName != null) {
            return personService.getPerson(assigneeName);
        } else {
            return null;
        }
    }

    private NodeRef getAssigneeManager(WorkflowTask task) {
        String assigneeName = (String) task.getProperties().get(ContentModel.PROP_OWNER);
        if (assigneeName != null) {
            String assigneeManager = orgStructService.getUserManager(assigneeName);
            if (assigneeManager != null) {
                return authorityService.getAuthorityNodeRef(assigneeManager);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private NodeRef getDocument(WorkflowTask task, NodeInfo nodeInfo) {
        Map<QName, List<NodeRef>> targetAssocs = nodeInfo.getTargetAssocs();
        NodeRef packageNode = getPackage(targetAssocs);
        if (packageNode != null && nodeService.exists(packageNode)) {
            List<ChildAssociationRef> packageAssocs = nodeService.getChildAssocs(packageNode);
            for (ChildAssociationRef packageAssoc : packageAssocs) {
                QName assocType = packageAssoc.getTypeQName();
                if (ContentModel.ASSOC_CONTAINS.equals(assocType) || WorkflowModel.ASSOC_PACKAGE_CONTAINS.equals(assocType)) {
                    return packageAssoc.getChildRef();
                }
            }
        }
        return null;
    }

    private NodeRef getPackage(Map<QName, List<NodeRef>> assoc) {
        for (QName qName : assoc.keySet()) {
            if (qName.equals(WorkflowModel.ASSOC_PACKAGE)) {
                List<NodeRef> packageNodeRefs = assoc.get(qName);
                if (!packageNodeRefs.isEmpty()) {
                    return packageNodeRefs.get(0);
                }
            }
        }
        return null;
    }

    private String getWorkflowName(WorkflowTask task, NodeInfo nodeInfo) {
        Map<QName, List<NodeRef>> targetAssocs = nodeInfo.getTargetAssocs();
        NodeRef packageNode = getPackage(targetAssocs);
        if (packageNode != null && nodeService.exists(packageNode)) {
            return (String) nodeService.getProperty(packageNode, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME);
        }
        return null;
    }

    private NodeRef getWorkflowInitiator(WorkflowTask task, NodeInfo nodeInfo) {
        Map<QName, List<NodeRef>> targetAssocs = nodeInfo.getTargetAssocs();
        NodeRef packageNode = getPackage(targetAssocs);
        if (packageNode != null && nodeService.exists(packageNode)) {
            String initiatorName = (String) nodeService.getProperty(packageNode, ContentModel.PROP_CREATOR);
            if (initiatorName != null) {
                return personService.getPerson(initiatorName);
            }
        }
        return null;
    }

    private MLText getMlDocumentTypeTitle(NodeRef documentRef) {
        QName documentType = nodeService.getType(documentRef);
        TypeDefinition typeDef = dictionaryService.getType(documentType);
        MLText mlText = new MLText();
        putDocTypeTitleForLocale(typeDef, "ru", mlText);
        putDocTypeTitleForLocale(typeDef, "en", mlText);
        return mlText;
    }

    private void putDocTypeTitleForLocale(TypeDefinition typeDef, String language, MLText mlText) {
        Locale locale = new Locale.Builder().setLanguage(language).build();
        String title = M2Label.getLabel(locale, typeDef.getModel(), messageLookup, "type", typeDef.getName(), "title");
        mlText.addValue(locale, title);
    }

    private NodeRef getDocumentKind(NodeRef documentRef) {
        if (documentRef != null && nodeService.exists(documentRef)) {
            Serializable documentKind = nodeService.getProperty(documentRef, ClassificationModel.PROP_DOCUMENT_KIND);
            if (documentKind != null) {
                return (NodeRef) documentKind;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private NodeRef getCounterparty(NodeRef document) {
        NodeRef counterparty = null;
        QName documentType = nodeService.getType(document);

        Map<String, String> mapping = documentToCounterparty.getMapping();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            QName docType = QName.resolveToQName(namespaceService, entry.getKey());
            QName assocType = QName.resolveToQName(namespaceService, entry.getValue());

            if (dictionaryService.isSubClass(documentType, docType)) {
                counterparty = RepoUtils.getFirstTargetAssoc(document, assocType, nodeService);
            }
        }

        return counterparty;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }

    public void setTaskMirrorRoot(NodeRef taskMirrorRoot) {
        this.taskMirrorRoot = taskMirrorRoot;
    }

    public void setTaskMirrorAssoc(QName taskMirrorAssoc) {
        this.taskMirrorAssoc = taskMirrorAssoc;
    }

    public void setOrgStructService(OrgStructService orgStructService) {
        this.orgStructService = orgStructService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    public void setMessageLookup(MessageLookup messageLookup) {
        this.messageLookup = messageLookup;
    }

    public void setMlAwareNodeService(NodeService mlAwareNodeService) {
        this.mlAwareNodeService = mlAwareNodeService;
    }

    public void setDocumentToCounterparty(MappingRegistry documentToCounterparty) {
        this.documentToCounterparty = documentToCounterparty;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
