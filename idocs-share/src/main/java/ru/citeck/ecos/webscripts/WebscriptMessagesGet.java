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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Registry;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class WebscriptMessagesGet extends DeclarativeWebScript 
{
	private static final String PARAM_WEBSCRIPT_ID = "webscript_id";
	private static final String PARAM_RESOURCE_BUNDLE = "resource_bundle";
	private static final String KEY_MESSAGES = "webscriptMessages";
	private static final String KEY_SCOPE = "scope";
	private static final String PARAM_SCOPE = "scope";

	private Registry registry;

	@Override
	public Map<String,Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
	{
		Map<String,String> templateArgs = req.getServiceMatch().getTemplateVars();
		String id = templateArgs.get(PARAM_WEBSCRIPT_ID);
		String resourceBundleName = req.getParameter(PARAM_RESOURCE_BUNDLE);
		if(id == null && resourceBundleName == null) {
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage(PARAM_WEBSCRIPT_ID + " or " + PARAM_RESOURCE_BUNDLE + " should be set");
			status.setRedirect(true);
			return null;
		}

		String scope = req.getParameter(PARAM_SCOPE);
		ResourceBundle resources = null;
		if (id != null) {
			WebScript webscript = registry.getWebScript(id);
			if(webscript == null) {
				status.setCode(Status.STATUS_NOT_FOUND);
				status.setMessage("Can not find webscript with id " + id);
				status.setRedirect(true);
				return null;
			}
			resources = webscript.getResources();
		}
		else {
			try {
				Locale locale = I18NUtil.getLocale();
				resources = ResourceBundle.getBundle(resourceBundleName, locale);
			} catch (Exception e) {
				status.setCode(Status.STATUS_NOT_FOUND);
				status.setMessage("Can not get resource bundle " + resourceBundleName);
				status.setRedirect(true);
				return null;
			}
		}

		if(resources == null) {
			return null;
		}

		Enumeration<String> keys = resources.getKeys();
		Map<String,Object> model = new HashMap<String,Object>();
		Map<String,Object> messages = new HashMap<String,Object>();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			messages.put(key, resources.getString(key));
		}
		model.put(KEY_MESSAGES, messages);
		model.put(KEY_SCOPE, scope);
		return model;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
}
