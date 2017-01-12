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
package ru.citeck.ecos.webscripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.surf.config.ResourceResolver;

public class ModulesInfoGet extends DeclarativeWebScript
{
	private static final String KEY_MODULES = "modules";
	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_VERSION = "version";
	private static final String KEY_INSTALL_DATE = "installDate";
	private static final String KEY_INSTALL_STATE = "installState";

	private static final String PROP_ID = "module.id";
	private static final String PROP_TITLE = "module.title";
	private static final String PROP_DESCRIPTION = "module.description";
	private static final String PROP_VERSION = "module.version";
	private static final String PROP_INSTALL_DATE = "module.installDate";
	private static final String PROP_INSTALL_STATE = "module.installState";
	
	private ResourceResolver resourceResolver;

	@Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
		try {
			Resource[] moduleDescriptors = resourceResolver.getResources("classpath:alfresco/module/*/module.properties");
			List<Object> modules = new ArrayList<Object>(moduleDescriptors.length);
			for(int i = 0; i < moduleDescriptors.length; i++) {
				Properties moduleProperties = new Properties();
				moduleProperties.load(moduleDescriptors[i].getInputStream());
				Map<String,Object> module = new TreeMap<String,Object>();
				module.put(KEY_ID, moduleProperties.getProperty(PROP_ID));
				module.put(KEY_TITLE, moduleProperties.getProperty(PROP_TITLE));
				module.put(KEY_DESCRIPTION, moduleProperties.getProperty(PROP_DESCRIPTION));
				module.put(KEY_VERSION, moduleProperties.getProperty(PROP_VERSION));
				module.put(KEY_INSTALL_DATE, moduleProperties.getProperty(PROP_INSTALL_DATE));
				module.put(KEY_INSTALL_STATE, moduleProperties.getProperty(PROP_INSTALL_STATE));
				modules.add(module);
			}
			
			Map<String,Object> model = new TreeMap<String,Object>();
			model.put(KEY_MODULES, modules);
			return model;
			
		} catch (IOException e) {
			throw new WebScriptException(e.getMessage(), e);
		}
		
	}

	public void setResourceResolver(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}
}
