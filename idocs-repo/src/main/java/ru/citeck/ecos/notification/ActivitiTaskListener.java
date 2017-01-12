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
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

public class ActivitiTaskListener
	implements TaskListener
{
		// DelegateTaskNotificationSender
	private DelegateTaskNotificationSender sender;
	protected boolean enabled;

	public void notify(DelegateTask task)
	{
		if(enabled)
		{
		String taskFormKey = getFormKey(task);
		if (taskFormKey != null)
		{
			task.setVariableLocal("taskFormKey", taskFormKey);
		}
		ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
		Boolean value = (Boolean)executionEntity.getVariable("cwf_sendNotification");
		
		if (Boolean.TRUE.equals(value))
		{
			sender.sendNotification(task);
		}
	}
	}
	private String getFormKey(DelegateTask task)
	{
		FormData formData = null;
		TaskEntity taskEntity = (TaskEntity)task;
		TaskFormHandler taskFormHandler = taskEntity.getTaskDefinition().getTaskFormHandler();
		if (taskFormHandler != null)
		{
			formData = taskFormHandler.createTaskForm(taskEntity);
			if (formData != null) return formData.getFormKey();
		}
		return null;
	}

	/**
	 * Set DelegateTaskNotificationSender.
	 * @param sender
	 */
	public void setSender(DelegateTaskNotificationSender sender) {
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

