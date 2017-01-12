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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.service.ServiceRegistry;

public class CancelWorkflowListener
	implements ExecutionListener
{
		// NotificationSender
	private ExecutionEntityNotificationSender sender;
	protected ServiceRegistry serviceRegistry;
	protected boolean enabled;

	public void notify(DelegateExecution task)
	{
		if(enabled)
		{
			ExecutionEntity entity = (ExecutionEntity)task;
			Boolean value = (Boolean)entity.getVariable("cwf_sendNotification");
			if (Boolean.TRUE.equals(value))
			{
				if(entity.isDeleteRoot() && "cancelled".equals(entity.getDeleteReason()))
				{
					sender.sendNotification((ExecutionEntity)task);
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
	public void setSender(ExecutionEntityNotificationSender sender) {
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