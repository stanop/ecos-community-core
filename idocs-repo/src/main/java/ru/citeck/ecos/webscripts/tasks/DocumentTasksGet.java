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
package ru.citeck.ecos.webscripts.tasks;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.service.AlfrescoServices;

import java.io.Serializable;
import java.util.*;


/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 * @author Sergey Tiunov <sergey.tiunov@citeck.ru>
 */
public class DocumentTasksGet extends DeclarativeWebScript {

    private static final String PARAM_NODEREF = "nodeRef";
    private static final String MODEL_TASK_ID = "taskId";
    private static final String MODEL_TASK_TYPE = "taskType";
    private static final String MODEL_START_DATE = "startDate";
    private static final String MODEL_DUE_DATE = "dueDate";
    private static final String MODEL_SENDER = "sender";
    private static final String MODEL_LAST_COMMENT = "lastcomment";
    private static final String MODEL_OUTCOME_PROP = "outcomeProperty";
    private static final String MODEL_OUTCOMES = "outcomes";
    private static final String MODEL_IS_REASSIGNABLE = "reassignable";
    private static final String MODEL_IS_RELEASABLE = "releasable";
    private static final String MODEL_IS_CLAIMABLE = "claimable";
    private static final QName MODEL_CLAIM_OWNER_PROP = QName.createQName(null, "claimOwner");

    private NodeService nodeService;

    private AuthorityService authorityService;

    private AuthenticationService authenticationService;

    private DictionaryService dictionaryService;
    
    private WorkflowService workflowService;

    private MessageService messageService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache) {
        String nodeRefParam = request.getParameter(PARAM_NODEREF);
        if (nodeRefParam == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "NodeRef parameter is mandatory");
            return null;
        }
        NodeRef nodeRef = new NodeRef(nodeRefParam);
        if (!nodeService.exists(nodeRef)) {
            status.setCode(Status.STATUS_NOT_FOUND, "Can not find node " + nodeRef);
            return null;
        }
        
        List<WorkflowTask> tasks = getDocumentTasks(nodeRef);
        
        List<Map<String, Object>> model = new ArrayList<>(tasks.size());
        for(WorkflowTask task : tasks) {
            model.add(generateTaskModel(task));
        }
        
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("tasks", model);
        return result;
    }

    private List<WorkflowTask> getDocumentTasks(NodeRef nodeRef) {
        List<WorkflowTask> tasks = new LinkedList<>();
        
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        Set<NodeRef> authorities = getCurrentUserAuthorities();
        
        for(WorkflowInstance workflow : getDocumentWorkflows(nodeRef)) {
            for(WorkflowTask task : getActiveWorkflowTasks(workflow)) {
                if(isTaskActor(task, userName, authorities)) {
                    tasks.add(task);
                }
            }
        }
        
        return tasks;
    }

    private List<WorkflowInstance> getDocumentWorkflows(NodeRef nodeRef) {
        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
        return workflows;
    }

    private boolean isTaskActor(WorkflowTask task, String userName,
            Set<NodeRef> authorities) {
        boolean matches;
        Map<QName, Serializable> properties = task.getProperties();
        String actor = (String) properties.get(ContentModel.PROP_OWNER);
        List<?> pooledActors = (List<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        
        if(actor != null) {
            matches = actor.equals(userName);
        } else {
            matches = pooledActors != null && CollectionUtils.intersection(pooledActors, authorities).size() > 0;
        }
        return matches;
    }

    private List<WorkflowTask> getActiveWorkflowTasks(WorkflowInstance workflow) {
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setActive(true);
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        query.setOrderBy(new OrderBy[]{OrderBy.TaskDue_Asc});
        query.setProcessId(workflow.getId());
        List<WorkflowTask> workflowTasks = workflowService.queryTasks(query, true);
        return workflowTasks;
    }

    private Set<NodeRef> getCurrentUserAuthorities() {
        String userName = authenticationService.getCurrentUserName();
        Set<String> groups = authorityService.getAuthorities();
        Set<NodeRef> authorities = new HashSet<>(groups.size() + 1);
        for(String group : groups) {
            authorities.add(authorityService.getAuthorityNodeRef(group));
        }
        authorities.add(authorityService.getAuthorityNodeRef(userName));
        return authorities;
    }
    
    private Map<String, Object> generateTaskModel(WorkflowTask task) {
        Map<QName, Serializable> properties = task.getProperties();
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_TASK_ID, task.getId());
        model.put(MODEL_TASK_TYPE, task.getName());
        model.put(MODEL_START_DATE, properties.get(WorkflowModel.PROP_START_DATE));
        model.put(MODEL_DUE_DATE, properties.get(WorkflowModel.PROP_DUE_DATE));
        model.put(MODEL_SENDER, properties.get(CiteckWorkflowModel.PROP_SENDER_NAME));
        model.put(MODEL_LAST_COMMENT, properties.get(CiteckWorkflowModel.PROP_LASTCOMMENT));

        List<?> pooledActors = (List<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);

        boolean hasPooledActors = pooledActors != null && pooledActors.size() > 0;
        boolean hasOwner = properties.get(ContentModel.PROP_OWNER) != null;
        model.put(MODEL_IS_CLAIMABLE, isTaskClaimable(properties, hasOwner, hasPooledActors));
        model.put(MODEL_IS_RELEASABLE, isTaskReleasable(properties, hasOwner, hasPooledActors));
        model.put(MODEL_IS_REASSIGNABLE, isTaskReassignable(properties, hasOwner));

        QName outcomeProperty = (QName) properties.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
        if(outcomeProperty != null) {
            model.put(MODEL_OUTCOME_PROP, outcomeProperty);
            model.put(MODEL_OUTCOMES, getAllowedValues(outcomeProperty));
        } else {
            model.put(MODEL_OUTCOME_PROP, WorkflowModel.PROP_OUTCOME);
            model.put(MODEL_OUTCOMES, Collections.emptyMap());
        }
        return model;
    }

    private boolean isTaskClaimable(Map<QName, Serializable> properties, boolean hasOwner, boolean hasPooledActors) {
        boolean isAllowed = hasPooledActors && (!hasOwner && properties.get(MODEL_CLAIM_OWNER_PROP) == null);
        boolean isDisabled = Boolean.FALSE.equals(properties.get(CiteckWorkflowModel.PROP_IS_TASK_CLAIMABLE));
        return isAllowed && !isDisabled;
    }

    private boolean isTaskReleasable(Map<QName, Serializable> properties, boolean hasOwner, boolean hasPooledActors) {
        boolean isAllowed = hasPooledActors && (hasOwner || properties.get(MODEL_CLAIM_OWNER_PROP) != null);
        boolean isDisabled = Boolean.FALSE.equals(properties.get(CiteckWorkflowModel.PROP_IS_TASK_RELEASABLE));
        return isAllowed && !isDisabled;
    }

    private boolean isTaskReassignable(Map<QName, Serializable> properties, boolean hasOwner) {
        boolean isAllowed = Boolean.TRUE.equals(properties.get(WorkflowModel.PROP_REASSIGNABLE))
                && (hasOwner || properties.get(MODEL_CLAIM_OWNER_PROP) != null);
        boolean isDisabled = Boolean.FALSE.equals(properties.get(CiteckWorkflowModel.PROP_IS_TASK_REASSIGNABLE));
        return isAllowed && !isDisabled;
    }

    private Map<String, String> getAllowedValues(QName property) {
        PropertyDefinition propertyDef = dictionaryService.getProperty(property);
        return getAllowedValues(propertyDef);
    }

    private Map<String, String> getAllowedValues(PropertyDefinition propertyDefinition) {
        List<ConstraintDefinition> constraintDefinitions = propertyDefinition.getConstraints();
        Map<String, String> allowedValues = new LinkedHashMap<String, String>();
        for (ConstraintDefinition constraintDefinition : constraintDefinitions) {
            Constraint constraint = constraintDefinition.getConstraint();
            if (constraint instanceof ListOfValuesConstraint) {
                ListOfValuesConstraint listOfValues = (ListOfValuesConstraint) constraint;
                for (String allowedValue : listOfValues.getAllowedValues()) {
                    allowedValues.put(allowedValue, listOfValues.getDisplayLabel(allowedValue, messageService));
                }
            }
        }
        return allowedValues;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        authorityService = serviceRegistry.getAuthorityService();
        authenticationService = serviceRegistry.getAuthenticationService();
        dictionaryService = serviceRegistry.getDictionaryService();
        workflowService = serviceRegistry.getWorkflowService();
        messageService = (MessageService) serviceRegistry.getService(AlfrescoServices.MESSAGE_SERVICE);
    }
}
