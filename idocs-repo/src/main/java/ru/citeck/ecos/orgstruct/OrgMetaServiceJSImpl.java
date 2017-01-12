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
package ru.citeck.ecos.orgstruct;

import java.util.List;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapNode;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapNodes;

/**
 * OrgMetaService JavaScript API.
 * Wraps all requests to OrgMetaService Java API.
 * 
 * @author Sergey Tiunov
 *
 */
public class OrgMetaServiceJSImpl extends AlfrescoScopableProcessorExtension implements OrgMetaServiceJS
{
	private OrgMetaService orgMetaService;

	@Override
	public ScriptNode createSubType(String type, String name) {
		return wrapNode(orgMetaService.createSubType(type, name), this);
	}

	@Override
	public ScriptNode getSubType(String type, String name) {
		return wrapNode(orgMetaService.getSubType(type, name), this);
	}

	@Override
	public void deleteSubType(String type, String name) {
		orgMetaService.deleteSubType(type, name);
	}

	@Override
	public ScriptNode[] getAllSubTypes(String type) {
		return wrapNodes(orgMetaService.getAllSubTypes(type), this);
	}

	@Override
	public String[] getGroupTypes() {
		List<String> groupTypes = orgMetaService.getGroupTypes();
		return groupTypes.toArray(new String[groupTypes.size()]);
	}

	@Override
	public ScriptNode getSubTypeRoot(String type) {
		return wrapNode(this.orgMetaService.getSubTypeRoot(type), this);
	}

	public void setOrgMetaService(OrgMetaService orgMetaService) {
		this.orgMetaService = orgMetaService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
