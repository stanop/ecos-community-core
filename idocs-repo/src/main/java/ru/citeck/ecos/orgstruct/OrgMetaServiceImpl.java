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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * OrgMetaService implementation.
 * Contains registry of GroupSubTypeDAO.
 * Delegates all requests to corresponding DAO.
 * 
 * @author Sergey Tiunov
 *
 */
public class OrgMetaServiceImpl implements OrgMetaService
{
	private Map<String,GroupSubTypeDAO> components;
	
	private GroupSubTypeDAO getComponent(String type) {
		if(!components.containsKey(type)) {
			throw new IllegalArgumentException("No such group type: " + type);
		}
		return components.get(type);
	}
	
	public void setComponents(Map<String,GroupSubTypeDAO> components) {
		this.components = components;
	}

	@Override
	public NodeRef getSubType(String type, String name) {
		return getComponent(type).getSubType(name);
	}

	@Override
	public List<NodeRef> getAllSubTypes(String type) {
		return getComponent(type).getAllSubTypes();
	}

	@Override
	public NodeRef createSubType(String type, String name) {
		return getComponent(type).createSubType(name);
	}

	@Override
	public void deleteSubType(String type, String name) {
		getComponent(type).deleteSubType(name);
	}

	@Override
	public List<String> getGroupTypes() {
		List<String> groupTypes = new ArrayList<String>();
		groupTypes.addAll(components.keySet());
		return groupTypes;
	}

	@Override
	public NodeRef getSubTypeRoot(String type) {
		return getComponent(type).getSubTypeRoot();
	}

}
