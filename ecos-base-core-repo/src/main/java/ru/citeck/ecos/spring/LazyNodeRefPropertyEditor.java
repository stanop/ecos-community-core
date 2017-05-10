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
package ru.citeck.ecos.spring;

import java.beans.PropertyEditorSupport;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.citeck.ecos.utils.LazyNodeRef;

public class LazyNodeRefPropertyEditor extends PropertyEditorSupport implements ApplicationContextAware
{
	private ApplicationContext applicationContext;
	private String nodeServiceName;
	private String searchServiceName;
    private String namespaceServiceName;
	
	public void setAsText(String text) {
		LazyNodeRef value = new LazyNodeRef(applicationContext, text);
		value.setNodeServiceName(nodeServiceName);
		value.setSearchServiceName(searchServiceName);
		value.setNamespaceServiceName(namespaceServiceName);
		setValue(value);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setNodeServiceName(String nodeServiceName) {
		this.nodeServiceName = nodeServiceName;
	}

	public void setSearchServiceName(String searchServiceName) {
		this.searchServiceName = searchServiceName;
	}

    public void setNamespaceServiceName(String namespaceServiceName) {
        this.namespaceServiceName = namespaceServiceName;
    }

}
