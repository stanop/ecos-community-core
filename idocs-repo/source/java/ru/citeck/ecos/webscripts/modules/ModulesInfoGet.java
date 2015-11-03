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
package ru.citeck.ecos.webscripts.modules;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ModulesInfoGet extends DeclarativeWebScript
{
	private static final String KEY_MODULES = "modules";

	private ModuleService moduleService;

	@Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
		List<ModuleDetails> modules = moduleService.getAllModules();
		Map<String,Object> model = new TreeMap<String,Object>();
		model.put(KEY_MODULES, modules);
		return model;
	}

	public void setModuleService(ModuleService moduleService) {
		this.moduleService = moduleService;
	}
}
