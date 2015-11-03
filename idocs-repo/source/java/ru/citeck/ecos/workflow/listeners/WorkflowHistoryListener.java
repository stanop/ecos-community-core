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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.ReflectionUtils;
import ru.citeck.ecos.history.HistoryService;

public class WorkflowHistoryListener extends AbstractExecutionListener {

	private static final Log logger = LogFactory.getLog(WorkflowHistoryListener.class);
	
	private static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";
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

	private WorkflowQNameConverter qNameConverter;
	private String VAR_PACKAGE;
	
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
		if(initiatorRef != null) {
			initiator = (String) nodeService.getProperty(initiatorRef.getNodeRef(), ContentModel.PROP_USERNAME);
		}
		
		// workflow definition
		WorkflowDefinition workflowDefinition = null;
		Object workflowDefinitionId = ReflectionUtils.callGetterIfDeclared(execution, "getProcessDefinitionId", null);
		if(workflowDefinitionId != null) {
			workflowDefinition = workflowService.getDefinitionById(ACTIVITI_PREFIX + workflowDefinitionId);
			if(workflowDefinition == null) {
				logger.warn("Unknown workflow definition: " + workflowDefinitionId);
				return;
			}
		}
		
		// document
		NodeRef document = null;
		NodeRef wfPackage = ((ScriptNode) execution.getVariable(VAR_PACKAGE)).getNodeRef();
		if(wfPackage != null) {
			List<ChildAssociationRef> packageAssocs = nodeService.getChildAssocs(wfPackage, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			if(packageAssocs != null && packageAssocs.size() > 0) {
				document = packageAssocs.get(0).getChildRef();
			}
		}

		// persist it
		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(5);
		eventProperties.put(HistoryModel.PROP_NAME, eventName);
		eventProperties.put(HistoryModel.PROP_WORKFLOW_INSTANCE_ID, ACTIVITI_PREFIX + execution.getProcessInstanceId());
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
		
		qNameConverter = new WorkflowQNameConverter(namespaceService);
		VAR_PACKAGE = qNameConverter.mapQNameToName(WorkflowModel.ASSOC_PACKAGE);
	}

}
