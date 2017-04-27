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
package ru.citeck.ecos.notification;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;

import java.util.Map;

public class CompleteTaskListener
	implements TaskListener
{
		// NotificationSender
	private AbstractNotificationSender<DelegateTask> sender;
	protected ServiceRegistry serviceRegistry;
    private PersonService personService;
    private AuthenticationService authenticationService;
	protected NodeService nodeService;
	private Map<String, Map<String,String>> conditions;
	protected NamespaceService namespaceService;
	protected WorkflowQNameConverter qNameConverter;
	protected boolean enabled;

	public void notify(DelegateTask task)
	{
		if(enabled)
		{
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		Boolean value = (Boolean)executionEntity.getVariable("cwf_sendNotification");
		
		if (Boolean.TRUE.equals(value))
		{
		WorkflowTask wfTask = serviceRegistry.getWorkflowService().getTaskById("activiti$"+task.getId());
		nodeService = serviceRegistry.getNodeService();
		namespaceService = serviceRegistry.getNamespaceService();
		qNameConverter = new WorkflowQNameConverter(namespaceService);
		if(conditions!=null && wfTask!=null)
		{
			Map <String,String> condition = conditions.get(wfTask.getName());
			int result = 0;
			if(condition!=null && condition.size()>0)
			{
				for (Map.Entry<String,String> entry : condition.entrySet()) {
					String actualValue = (String) wfTask.getProperties().get(qNameConverter.mapNameToQName(entry.getKey()));
					if(!actualValue.equals(entry.getValue()))
						result++;
				}
			}
			if(result==0)
				sender.sendNotification(task);
		}
		else
		{
			sender.sendNotification(task);
		}
		}
	}
	}
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * Set NotificationSender.
	 * @param sender
	 */
	public void setSender(AbstractNotificationSender<DelegateTask> sender) {
		this.sender = sender;
	}
	
	/**
	* Conditions provided as parameter conditions: "task name"-{"field"-"value", ...}
	* @param task subscribers
	*/
	public void setConditions(Map<String, Map<String,String>> conditions) {
    	this.conditions = conditions;
    }
	
	/**
	* enabled
	* @param true or false
	*/
	public void setEnabled(Boolean enabled) {
    	this.enabled = enabled.booleanValue();
    }
}

