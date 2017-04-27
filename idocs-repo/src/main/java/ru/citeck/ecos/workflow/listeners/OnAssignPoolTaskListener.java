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

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;

import ru.citeck.ecos.notification.NotificationSender;

/**
 * This task listener send notification then task move to pool.
 * 
 * @author Elena Zaripova
 */
public class OnAssignPoolTaskListener implements TaskListener 
{
	protected ServiceRegistry serviceRegistry;
	protected WorkflowService workflowService;
	protected NotificationSender<WorkflowTask> sender;
	protected boolean enabled;

    @Override
	public void notify(DelegateTask task) {
		if(enabled)
		{
			workflowService = serviceRegistry.getWorkflowService();
			WorkflowTask wftask = workflowService.getTaskById("activiti$"+task.getId());
			if(task.getAssignee()==null)
			{
				if(wftask!=null)
				{
					sender.sendNotification(wftask);
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
	public void setSender(NotificationSender<WorkflowTask> sender) {
		this.sender = sender;
	}
	/**
	* enabled
	* @param true or false
	*/
	public void setEnabled(Boolean enabled) {
    	this.enabled = enabled.booleanValue();
    }

}
