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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.deputy.DeputyService;
import ru.citeck.ecos.history.HistoryEventType;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.CasePerformModel;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.service.CiteckServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskHistoryListener extends AbstractTaskListener {

	private static final Log logger = LogFactory.getLog(TaskHistoryListener.class);
	
	public static final String VAR_ADDITIONAL_EVENT_PROPERTIES = "event_additionalProperties";
	private static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";
	private static final Map<String, String> eventNames;
	
	static {
		eventNames = new HashMap<String, String>(3);
		eventNames.put(TaskListener.EVENTNAME_CREATE, HistoryEventType.TASK_CREATE);
		eventNames.put(TaskListener.EVENTNAME_ASSIGNMENT, HistoryEventType.TASK_ASSIGN);
		eventNames.put(TaskListener.EVENTNAME_COMPLETE, HistoryEventType.TASK_COMPLETE);
	}
	
	private NodeService nodeService;
	private HistoryService historyService;
	private NamespaceService namespaceService;
	private AuthorityService authorityService;
	private DeputyService deputyService;
	private CaseRoleService caseRoleService;
	private List<String> panelOfAuthorized; //группа уполномоченных

	private WorkflowQNameConverter qNameConverter;
	private String VAR_OUTCOME_PROPERTY_NAME, VAR_COMMENT, VAR_DESCRIPTION;
	
	/* (non-Javadoc)
	 * @see ru.citeck.ecos.workflow.listeners.AbstractTaskListener#notifyImpl(org.activiti.engine.delegate.DelegateTask)
	 */
	@Override
	protected void notifyImpl(DelegateTask task) {

		// event
		String eventName = eventNames.get(task.getEventName());
		if(eventName == null) {
			logger.warn("Unsupported activiti task event: " + task.getEventName());
			return;
		}
		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>();
		// task type
		QName taskType = QName.createQName((String) task.getVariable(ActivitiConstants.PROP_TASK_FORM_KEY), namespaceService);

		// task outcome
		QName outcomeProperty = (QName) task.getVariable(VAR_OUTCOME_PROPERTY_NAME);
		if(outcomeProperty == null) {
			outcomeProperty = WorkflowModel.PROP_OUTCOME;
		}
		String taskOutcome = (String) task.getVariable(qNameConverter.mapQNameToName(outcomeProperty));

        // task comment
        String taskComment = (String) task.getVariable(VAR_COMMENT);

        // task attachments
        ArrayList<NodeRef> taskAttachments = ListenerUtils.getTaskAttachments(task);

		// task assignee
		String assignee = task.getAssignee();

		String originalOwner = (String) task.getVariableLocal("taskOriginalOwner");

		if (assignee != null && !assignee.equals(originalOwner)) {
			if (originalOwner != null && deputyService.isAssistantUserByUser(originalOwner, assignee)) {
				eventProperties.put(QName.createQName("", "taskOriginalOwner"), authorityService.getAuthorityNodeRef(originalOwner));
			}
		}

		// pooled actors
		ArrayList<NodeRef> pooledActors = ListenerUtils.getPooledActors(task, authorityService);

		// document
		NodeRef document = ListenerUtils.getDocument(task.getExecution(), nodeService);

		// additional properties if any
		Map<QName, Serializable> additionalProperties = getAdditionalProperties(task.getExecution());

		// persist it

        if(additionalProperties != null) {
            eventProperties.putAll(additionalProperties);
        }
		NodeRef bpmPackage = ListenerUtils.getWorkflowPackage(task);
		List<AssociationRef> packageAssocs = nodeService.getSourceAssocs(bpmPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);

		String roleName;
		if (panelOfAuthorized != null && assignee != null && !panelOfAuthorized.isEmpty() && panelOfAuthorized.size() > 0) {
			List<NodeRef> listRoles = getListRoles(document);
			roleName = getAuthorizedName(panelOfAuthorized, listRoles, assignee) != null ? getAuthorizedName(panelOfAuthorized, listRoles, assignee) : getRoleName(packageAssocs, assignee);
		} else {
			roleName = getRoleName(packageAssocs, assignee);
			if (packageAssocs.size() > 0) {
				eventProperties.put(HistoryModel.PROP_CASE_TASK, packageAssocs.get(0).getSourceRef());
			}
		}

		eventProperties.put(HistoryModel.PROP_NAME, eventName);
		eventProperties.put(HistoryModel.PROP_TASK_INSTANCE_ID, ACTIVITI_PREFIX + task.getId());
		eventProperties.put(HistoryModel.PROP_TASK_TYPE, taskType);
		eventProperties.put(HistoryModel.PROP_TASK_OUTCOME, taskOutcome);
		eventProperties.put(HistoryModel.PROP_TASK_COMMENT, taskComment);
		eventProperties.put(HistoryModel.PROP_TASK_ATTACHMENTS, taskAttachments);
		eventProperties.put(HistoryModel.PROP_TASK_POOLED_ACTORS, pooledActors);
		eventProperties.put(HistoryModel.PROP_TASK_ROLE, roleName);

		eventProperties.put(HistoryModel.PROP_WORKFLOW_INSTANCE_ID, ACTIVITI_PREFIX + task.getProcessInstanceId());
		eventProperties.put(HistoryModel.PROP_WORKFLOW_DESCRIPTION, (Serializable) task.getExecution().getVariable(VAR_DESCRIPTION));
		eventProperties.put(HistoryModel.ASSOC_INITIATOR, assignee != null ? assignee : HistoryService.SYSTEM_USER);
		eventProperties.put(HistoryModel.ASSOC_DOCUMENT, document);
		historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
	}

    @SuppressWarnings("rawtypes")
    private Map<QName, Serializable> getAdditionalProperties(DelegateExecution execution) {
        Object additionalPropertiesObj = execution.getVariable(VAR_ADDITIONAL_EVENT_PROPERTIES);
        if(additionalPropertiesObj == null) {
            return null;
        }
        if(additionalPropertiesObj instanceof Map) {
            return convertProperties((Map) additionalPropertiesObj);
        }
        logger.warn("Unknown type of additional event properties: " + additionalPropertiesObj.getClass());
        return null;
    }

    @SuppressWarnings("rawtypes")
    private Map<QName, Serializable> convertProperties(Map additionalProperties) {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(additionalProperties.size());
        for(Object key : additionalProperties.keySet()) {
            QName name = null;
            if(key instanceof String) {
                name = qNameConverter.mapNameToQName((String) key);
            } else if(key instanceof QName) {
                name = (QName) key;
            } else {
                logger.warn("Unknown type of key: " + key.getClass());
                continue;
            }
            result.put(name, (Serializable) additionalProperties.get(key));
        }
        return result;
    }

    private List<NodeRef> getListRoles(NodeRef document) {
		List<ChildAssociationRef> childsAssocRefs = nodeService.getChildAssocs(document, ICaseRoleModel.ASSOC_ROLES, RegexQNamePattern.MATCH_ALL);
		List<NodeRef> roles = new ArrayList<>();
		for (ChildAssociationRef childAssociationRef: childsAssocRefs) {
			roles.add(childAssociationRef.getChildRef());
		}
		return roles;
	}

	private String getAuthorizedName(List<String> varNameRoles, List<NodeRef> listRoles, String assignee) {
		for (NodeRef role: listRoles) {
			if (varNameRoles.contains(nodeService.getProperty(role, ICaseRoleModel.PROP_VARNAME))) {
				for(String varNameRole: varNameRoles) {
					if (varNameRole.equals(nodeService.getProperty(role, ICaseRoleModel.PROP_VARNAME))) {
						Map<NodeRef, NodeRef> delegates = caseRoleService.getDelegates(role);
						for (Map.Entry<NodeRef, NodeRef> entry : delegates.entrySet()) {
							if (authorityService.getAuthorityNodeRef(assignee).equals(entry.getValue())) {
								return (String) nodeService.getProperty(entry.getKey(), ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
							}
						}
					}
				}
			}
		}
		return null;
	}

	private String getRoleName(List<AssociationRef> packageAssocs, String assignee) {
		String roleName = "";
		if (packageAssocs.size() > 0) {
			NodeRef currentTask = packageAssocs.get(0).getSourceRef();
			List<AssociationRef> performerRoles = nodeService.getTargetAssocs(currentTask, CasePerformModel.ASSOC_PERFORMERS_ROLES);
			if (performerRoles != null && !performerRoles.isEmpty()) {
				NodeRef firstRole = performerRoles.get(0).getTargetRef();
				roleName = (String) nodeService.getProperty(firstRole, ContentModel.PROP_NAME);
			}
		}
		if (roleName.isEmpty()) {
			roleName = assignee;
		}
		return roleName;
	}

    @Override
	protected void initImpl() {
		historyService = (HistoryService) serviceRegistry.getService(CiteckServices.HISTORY_SERVICE);
		nodeService = serviceRegistry.getNodeService();
		namespaceService = serviceRegistry.getNamespaceService();
		authorityService = serviceRegistry.getAuthorityService();
		deputyService = (DeputyService) serviceRegistry.getService(CiteckServices.DEPUTY_SERVICE);
		caseRoleService = (CaseRoleService) serviceRegistry.getService(CiteckServices.CASE_ROLE_SERVICE);
		
		qNameConverter = new WorkflowQNameConverter(namespaceService);
		VAR_OUTCOME_PROPERTY_NAME = qNameConverter.mapQNameToName(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
		VAR_COMMENT = qNameConverter.mapQNameToName(WorkflowModel.PROP_COMMENT);
		VAR_DESCRIPTION = qNameConverter.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
	}

	public void setPanelOfAuthorized(List<String> panelOfAuthorized) {
		this.panelOfAuthorized = panelOfAuthorized;
	}
}
