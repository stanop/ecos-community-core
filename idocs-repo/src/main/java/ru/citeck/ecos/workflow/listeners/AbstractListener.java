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

import org.activiti.engine.impl.context.Context;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;

public abstract class AbstractListener
{
	protected ServiceRegistry serviceRegistry;
	
	protected Object getBean(String name) {
		return serviceRegistry.getService(QName.createQName(null, name));
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getBean(String name, Class<T> clazz) {
		Object bean = getBean(name);
		if(clazz.isInstance(bean)) {
			return (T) bean;
		} else {
			return null;
		}
	}
	
	protected <T> T getBean(Class<T> clazz) {
		return getBean(clazz.getSimpleName(), clazz);
	}
	
	protected final void init() {
		if(serviceRegistry == null) {
			serviceRegistry = (ServiceRegistry) Context.getProcessEngineConfiguration().getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
			this.initImpl();
		}
	}
	
	protected void initImpl() {
		// subclasses can override this
	}
	
}
