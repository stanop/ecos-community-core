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
import org.alfresco.model.ContentModel;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;

import ru.citeck.ecos.notification.NotificationSender;

/**
 * This task listener set last task owner and send notification if task is reassigned.
 * 
 * @author Elena Zaripova
 */
public class OnAssignTaskSetLastOwnerListener implements TaskListener 
{
	protected ServiceRegistry serviceRegistry;
	protected WorkflowService workflowService;
	protected NotificationSender<DelegateTask> sender;
	protected boolean enabled;

    @Override
	public void notify(DelegateTask task) {
		WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(serviceRegistry.getNamespaceService());
		String lastTaskOwnerVar = qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_LAST_TASK_OWNER);
		String owner = task.getAssignee();
		String originalOwner = (String) task.getVariable("taskOriginalOwner");

		String lastTaskOwner = (String) task.getVariable(lastTaskOwnerVar);
		if(enabled)
		{
			if((lastTaskOwner!=null && !lastTaskOwner.equals(owner)) || (lastTaskOwner==null && owner!=null && originalOwner!=null && !owner.equals(originalOwner)))
			{
				sender.sendNotification(task);
			}
		}
		task.setVariableLocal(lastTaskOwnerVar, owner);
	}
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	/**
	 * Set NotificationSender.
	 * @param sender
	 */
	public void setSender(NotificationSender<DelegateTask> sender) {
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
