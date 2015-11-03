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
import org.activiti.engine.impl.context.Context;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;

public class TaskOutcomePush implements TaskListener 
{
	private String outcomePropertyName;
	
	public void setOutcomePropertyName(String outcomePropertyName) {
		this.outcomePropertyName = outcomePropertyName;
	}

	@Override
	public void notify(DelegateTask task) {
		DelegateExecution execution = task.getExecution();
		
		QName propertyQName = (QName) task.getVariable("bpm_outcomePropertyName");
		if(propertyQName == null) {
			return;
		}
		
		ServiceRegistry serviceRegistry = (ServiceRegistry) Context.getProcessEngineConfiguration()
				.getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
		
		String propertyName = propertyQName.toPrefixString(serviceRegistry.getNamespaceService()).replace(":", "_");
		Object outcome = task.getVariable(propertyName);
		execution.setVariable(outcomePropertyName, outcome);
	}

}
