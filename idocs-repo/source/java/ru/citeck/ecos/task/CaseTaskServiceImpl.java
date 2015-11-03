/*
 * Copyright (C) 2008-2015 Citeck LLC.
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
package ru.citeck.ecos.task;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.model.LifeCycleModel;
import ru.citeck.ecos.search.*;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Maxim Strizhov
 */
public class CaseTaskServiceImpl implements CaseTaskService {
    private static final Log log = LogFactory.getLog(CaseTaskService.class);
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private CriteriaSearchService criteriaSearchService;

    private static HashMap<String, String> performersMap = new HashMap<>();

    static {
        performersMap.put("activiti$print", "wfpr:printer");
        performersMap.put("activiti$scan", "wfscan:clerk");
        performersMap.put("activiti$sign", "wfsgn:signer");
        performersMap.put("activiti$correction", "wfcr:corrector");
        performersMap.put("activiti$registration", "wfrg:registrator");
        performersMap.put("activiti$normative-control", "wfnc:controller");
        performersMap.put("activiti$normative-control", "wfnc:controller");
        performersMap.put("activiti$prolongation", "wfprolong:prolongs");
        performersMap.put("activiti$simple-affirm", "wfsa:affirmAgent");
        performersMap.put("activiti$simple-payment", "wfsp:paymentAgent");
        performersMap.put("activiti$move-to-archive", "wfarc:archiver");
        performersMap.put("activiti$perform", "wfperf:performers");
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCriteriaSearchService(CriteriaSearchService criteriaSearchService) {
        this.criteriaSearchService = criteriaSearchService;
    }

    public void init() {
    }

    @Override
    public List<NodeRef> getTasks(NodeRef nodeRef) {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ICaseTaskModel.ASSOC_TASKS, RegexQNamePattern.MATCH_ALL);
        List<NodeRef> result = new ArrayList<>(assocs.size());
        for (ChildAssociationRef assoc : assocs) {
            result.add(assoc.getChildRef());
        }
        return result;
    }

    //    @Override
    public void startTaskOld(NodeRef taskNodeRef) {
        if (log.isDebugEnabled()) {
            log.debug("Start task " + taskNodeRef.toString());
        }
        if (!nodeService.exists(taskNodeRef)) {
            return;
        }
        log.info("Starting workflow from task: " + taskNodeRef.toString());
        NodeRef wfPackage = workflowService.createPackage(null);
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Тестовый процесс");

        String workflowDefinitionName = (String) nodeService.getProperty(taskNodeRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);
        log.info("Workflow name: " + workflowDefinitionName);
        List<ChildAssociationRef> variablesMappingRefs = nodeService.getChildAssocs(taskNodeRef, ICaseTaskModel.ASSOC_TASK_ROLES_MAPPING, RegexQNamePattern.MATCH_ALL);
        log.info("Mappings count: " + variablesMappingRefs.size());
        if (variablesMappingRefs != null && variablesMappingRefs.size() > 0) {
            for (ChildAssociationRef variableMappingRef : variablesMappingRefs) {
                String variableName = (String) nodeService.getProperty(variableMappingRef.getChildRef(), ICaseTaskModel.PROP_WORKFLOW_VARIABLE_NAME);
                log.info("Variable name: " + variableName);
                QName key = QName.createQName(variableName, namespaceService);
                log.info("Key = " + key);
                List<AssociationRef> roleAssociationRefs = nodeService.getTargetAssocs(variableMappingRef.getChildRef(), ICaseTaskModel.ASSOC_ROLE);
                log.info("Roles count: " + roleAssociationRefs.size());
                ArrayList<NodeRef> authorities = new ArrayList<>();
                for (AssociationRef roleAssociationRef : roleAssociationRefs) {
                    List<AssociationRef> roles = nodeService.getTargetAssocs(roleAssociationRef.getTargetRef(), ICaseRoleModel.ASSOC_ASSIGNEES);
                    for (AssociationRef roleRef : roles) {
                        authorities.add(roleRef.getTargetRef());
                    }
                }
                log.info("Authorities: " + authorities);
                workflowProps.put(key, authorities);
            }
        }
        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(workflowDefinitionName);
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), workflowProps);
        nodeService.setProperty(taskNodeRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, wfPath.getInstance().getId());
        nodeService.setProperty(taskNodeRef, ActivityModel.PROP_ACTUAL_START_DATE, new Date());
//        nodeService.setProperty(taskNodeRef, LifeCycleModel.PROP_STATE, "Star");
    }

    @Override
    public void startTask(NodeRef taskNodeRef) {
        if (!nodeService.exists(taskNodeRef)) {
            return;
        }
        log.info("Starting workflow from task: " + taskNodeRef.toString());
        String workflowDescription = (String) nodeService.getProperty(taskNodeRef, ContentModel.PROP_TITLE);
        Date workflowDueDate = (Date) nodeService.getProperty(taskNodeRef, ActivityModel.PROP_PLANNED_END_DATE);
        Integer workflowPriority = (Integer) nodeService.getProperty(taskNodeRef, ICaseTaskModel.PROP_PRIORITY);

        NodeRef wfPackage = workflowService.createPackage(null);
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModel.PROP_DUE_DATE, workflowDueDate);
        workflowProps.put(WorkflowModel.PROP_PRIORITY, workflowPriority);
        NodeRef parent = getNotActivityParent(taskNodeRef);

        this.nodeService.addChild(wfPackage, parent, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                        QName.createValidLocalName((String) this.nodeService.getProperty(parent, ContentModel.PROP_NAME))));

        String workflowDefinitionName = (String) nodeService.getProperty(taskNodeRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);
        String variableName = getPerformerVariableName(workflowDefinitionName);
        QName key = QName.createQName(variableName, namespaceService);
        List<AssociationRef> performersAssociationRefs = nodeService.getTargetAssocs(taskNodeRef, ICaseTaskModel.ASSOC_PERFORMER);
        ArrayList<NodeRef> authorities = new ArrayList<>();
        for (AssociationRef performerAssociationRef : performersAssociationRefs) {
            List<AssociationRef> authorityAssociationRefs = nodeService.getTargetAssocs(performerAssociationRef.getTargetRef(), ICaseRoleModel.ASSOC_ASSIGNEES);
            for (AssociationRef authorityAssociationRef : authorityAssociationRefs) {
                authorities.add(authorityAssociationRef.getTargetRef());
            }
        }
        workflowProps.put(key, authorities);
        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(workflowDefinitionName);
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), workflowProps);
        nodeService.setProperty(taskNodeRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, wfPath.getInstance().getId());
        nodeService.setProperty(taskNodeRef, LifeCycleModel.PROP_STATE, "Started");
        nodeService.setProperty(taskNodeRef, ActivityModel.PROP_ACTUAL_START_DATE, new Date());
    }

    @Override
    public void onTaskCompleted(NodeRef nodeRef, String processType, String processInstanceId) {
//        nodeService.setProperty(nodeRef, LifeCycleModel.PROP_STATE, "Completed");
        SearchCriteria searchCriteria = new SearchCriteria(namespaceService)
                .addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, ICaseTaskModel.TYPE_TASK)
                .addCriteriaTriplet(ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME, SearchPredicate.STRING_EQUALS, processType);
        CriteriaSearchResults searchResults = criteriaSearchService.query(searchCriteria, SearchService.LANGUAGE_LUCENE);
        List<NodeRef> results = searchResults.getResults();
        for (NodeRef result : results) {
            String workflowId = (String) nodeService.getProperty(result, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID);
            if (workflowId != null && workflowId.equals("activiti$" + processInstanceId)) {
                nodeService.setProperty(result, ActivityModel.PROP_ACTUAL_END_DATE, new Date());
                nodeService.setProperty(result, LifeCycleModel.PROP_STATE, "Completed");
            }
        }

    }

    private String getPerformerVariableName(String workflowName) {
        return performersMap.get(workflowName);
    }

    private NodeRef getNotActivityParent(NodeRef sourceRef) {
        ChildAssociationRef parent = nodeService.getPrimaryParent(sourceRef);
        log.info("Parent: " + parent.getParentRef());
        log.info("IsSubType: " + RepoUtils.isSubType(parent.getParentRef(), ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService));
        while (parent.getParentRef() != null
                && RepoUtils.isSubType(parent.getParentRef(), ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService)) {
            parent = nodeService.getPrimaryParent(parent.getParentRef());
            log.info(parent.getParentRef());
            log.info(RepoUtils.isSubType(parent.getParentRef(), ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService));
        }
        log.info("Found: " + parent.getParentRef());
        return parent.getParentRef();
    }
}
