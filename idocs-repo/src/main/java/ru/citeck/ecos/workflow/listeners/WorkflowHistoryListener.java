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
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.service.CiteckServices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkflowHistoryListener extends AbstractExecutionListener {

	private static final Log logger = LogFactory.getLog(WorkflowHistoryListener.class);
	
	private static final String ACTIVITI_PREFIX = ListenerUtils.ACTIVITI_PREFIX;
	private static final Map<String, String> eventNames;
	
	static {
		eventNames = new HashMap<String, String>(3);
		eventNames.put(ExecutionListener.EVENTNAME_START, "workflow.start");
		eventNames.put(ExecutionListener.EVENTNAME_END, "workflow.end");
	}
	
	private NodeService nodeService;
	private HistoryService historyService;
	private NamespaceService namespaceService;
	private WorkflowService workflowService;
	private WorkflowDocumentResolverRegistry documentResolverRegistry;

	private String VAR_COMMENT, VAR_DESCRIPTION;
	
	/* (non-Javadoc)
	 * @see ru.citeck.ecos.workflow.listeners.AbstractExecutionListener#notifyImpl(org.activiti.engine.delegate.DelegateExecution)
	 */
	@Override
	protected void notifyImpl(DelegateExecution execution) {

		// event
		String eventName = eventNames.get(execution.getEventName());
		if(eventName == null) {
			logger.warn("Unsupported activiti execution event: " + execution.getEventName());
			return;
		}
		ExecutionEntity entity = (ExecutionEntity)execution;
		if(entity.isDeleteRoot() && "cancelled".equals(entity.getDeleteReason()))
		{
			eventName+=".cancelled";
		}
		// initiator
		String initiator = null;
		ScriptNode initiatorRef = (ScriptNode) execution.getVariable(WorkflowConstants.PROP_INITIATOR);
		if(initiatorRef != null && nodeService.exists(initiatorRef.getNodeRef())) {
			initiator = (String) nodeService.getProperty(initiatorRef.getNodeRef(), ContentModel.PROP_USERNAME);
		}
		
		// workflow definition
		WorkflowDefinition workflowDefinition = null;
		Object workflowDefinitionId = ListenerUtils.tryGetProcessDefinitionId(execution);
		if (workflowDefinitionId != null) {
			workflowDefinition = ListenerUtils.tryGetWorkflowDefinition(execution, workflowService);
			if (workflowDefinition == null) {
				logger.warn("Unknown workflow definition: " + workflowDefinitionId);
				return;
			}
		}
		
		// document
		String workflowDefinitionName = null;
		if (workflowDefinition != null) {
			workflowDefinitionName = workflowDefinition.getName();
		}
		NodeRef document = documentResolverRegistry.getResolver(workflowDefinitionName).getDocument(execution);

		// persist it
		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(5);
		eventProperties.put(HistoryModel.PROP_NAME, eventName);
		eventProperties.put(HistoryModel.PROP_WORKFLOW_INSTANCE_ID, ACTIVITI_PREFIX + execution.getProcessInstanceId());
		eventProperties.put(HistoryModel.PROP_WORKFLOW_DESCRIPTION, (Serializable) execution.getVariable(VAR_DESCRIPTION));
		eventProperties.put(HistoryModel.PROP_TASK_COMMENT, (Serializable) execution.getVariable(VAR_COMMENT));
		if(workflowDefinition != null) {
			eventProperties.put(HistoryModel.PROP_WORKFLOW_TYPE, workflowDefinition.getName());
		}
		
		eventProperties.put(HistoryModel.ASSOC_INITIATOR, initiator != null ? initiator : HistoryService.SYSTEM_USER);
		eventProperties.put(HistoryModel.ASSOC_DOCUMENT, document);
		historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
	}

	@Override
	protected void initImpl() {
		historyService = (HistoryService) serviceRegistry.getService(CiteckServices.HISTORY_SERVICE);
		nodeService = serviceRegistry.getNodeService();
		namespaceService = serviceRegistry.getNamespaceService();
		workflowService = serviceRegistry.getWorkflowService();
		documentResolverRegistry = getBean(WorkflowDocumentResolverRegistry.BEAN_NAME, WorkflowDocumentResolverRegistry.class);
		
		WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
		VAR_COMMENT = qNameConverter.mapQNameToName(WorkflowModel.PROP_COMMENT);
		VAR_DESCRIPTION = qNameConverter.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
	}

}
