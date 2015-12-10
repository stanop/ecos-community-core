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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.history.HistoryService;

public class TaskHistoryListener extends AbstractTaskListener {

	private static final Log logger = LogFactory.getLog(TaskHistoryListener.class);
	
	public static final String VAR_ADDITIONAL_EVENT_PROPERTIES = "event_additionalProperties";
	private static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";
	private static final Map<String, String> eventNames;
	
	static {
		eventNames = new HashMap<String, String>(3);
		eventNames.put(TaskListener.EVENTNAME_CREATE, "task.create");
		eventNames.put(TaskListener.EVENTNAME_ASSIGNMENT, "task.assign");
		eventNames.put(TaskListener.EVENTNAME_COMPLETE, "task.complete");
	}
	
	private NodeService nodeService;
	private HistoryService historyService;
	private NamespaceService namespaceService;
	private AuthorityService authorityService;

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
		
		// pooled actors
		ArrayList<NodeRef> pooledActors = ListenerUtils.getPooledActors(task, authorityService);

		// document
		NodeRef document = ListenerUtils.getDocument(task.getExecution(), nodeService);
		
		// additional properties if any
		Map<QName, Serializable> additionalProperties = getAdditionalProperties(task.getExecution());
		
		// persist it
		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>();
        if(additionalProperties != null) {
            eventProperties.putAll(additionalProperties);
        }
		eventProperties.put(HistoryModel.PROP_NAME, eventName);
		eventProperties.put(HistoryModel.PROP_TASK_INSTANCE_ID, ACTIVITI_PREFIX + task.getId());
		eventProperties.put(HistoryModel.PROP_TASK_TYPE, taskType);
		eventProperties.put(HistoryModel.PROP_TASK_OUTCOME, taskOutcome);
		eventProperties.put(HistoryModel.PROP_TASK_COMMENT, taskComment);
		eventProperties.put(HistoryModel.PROP_TASK_ATTACHMENTS, taskAttachments);
		eventProperties.put(HistoryModel.PROP_TASK_POOLED_ACTORS, pooledActors);
		
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

    @Override
	protected void initImpl() {
		historyService = (HistoryService) serviceRegistry.getService(CiteckServices.HISTORY_SERVICE);
		nodeService = serviceRegistry.getNodeService();
		namespaceService = serviceRegistry.getNamespaceService();
		authorityService = serviceRegistry.getAuthorityService();
		
		qNameConverter = new WorkflowQNameConverter(namespaceService);
		VAR_OUTCOME_PROPERTY_NAME = qNameConverter.mapQNameToName(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
		VAR_OUTCOME_PROPERTY_NAME = qNameConverter.mapQNameToName(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
		VAR_COMMENT = qNameConverter.mapQNameToName(WorkflowModel.PROP_COMMENT);
		VAR_DESCRIPTION = qNameConverter.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
	}

}
