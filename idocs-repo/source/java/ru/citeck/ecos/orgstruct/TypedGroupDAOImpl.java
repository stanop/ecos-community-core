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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.utils.LazyQName;

/**
 * TypedGroupDAO aspect-based implementation.
 * Each typed group has aspect 'aspectName' 
 * and contains group sub-type name in text property 'propertyName'.
 * 
 * @author Sergey Tiunov
 *
 */
public class TypedGroupDAOImpl implements TypedGroupDAO {

	private NodeService nodeService;
	private NamespaceService namespaceService;
	private AuthorityService authorityService;
	private GroupSubTypeDAO subTypeDAO;
	
	private LazyQName aspectQName;
	private LazyQName propertyQName;
	
	private String aspectName, propertyName;
	
	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setSubTypeDAO(GroupSubTypeDAO metaComponent) {
		this.subTypeDAO = metaComponent;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public void init() {
		aspectQName = new LazyQName(namespaceService, aspectName);
		propertyQName = new LazyQName(namespaceService, propertyName);
	}
	
	/////////////////////////////////////////////////////////////////
	//               TypedGroupDAO IMPLEMENTATION                  //
	/////////////////////////////////////////////////////////////////

	@Override
	public boolean isTypedGroup(String name) {
		return isTypedGroup(getGroupNode(name));
	}

	@Override
	public String getGroupSubtype(String name) {
		NodeRef group = getGroupNode(name);
		if(!isTypedGroup(group)) {
			return null;
		}
		return getGroupSubtype(group);
	}

	@Override
	public List<String> getAllTypedGroups(boolean rootOnly) {
		Set<String> groups;
		if(rootOnly) {
			groups = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
		} else {
			groups = authorityService.getAllAuthorities(AuthorityType.GROUP);
		}
		List<String> typeGroups = new ArrayList<String>();
		for(String group : groups) {
			if(isTypedGroup(group)) {
				typeGroups.add(group);
			}
		}
		return typeGroups;
	}
	
	@Override
	public List<String> getAllTypedGroups(String subtype, boolean rootOnly) {
		List<String> allGroups = getAllTypedGroups(rootOnly);
		List<String> groups = new ArrayList<String>();
		for(String group : allGroups) {
			String groupSubtype = (String) nodeService.getProperty(getGroupNode(group), propertyQName.getQName());
			if(groupSubtype.equals(subtype)) {
				groups.add(group);
			}
		}
		return groups;
	}

	@Override
	public String createTypedGroup(String subtype, String name) {
		NodeRef subType = subTypeDAO.getSubType(subtype);
		if(subType == null) {
			throw new IllegalArgumentException("No such " + aspectQName.getQName().getPrefixString() + " sub-type: " + subtype);
		}
		
		NodeRef nodeRef = getGroupNode(name);
		if(nodeRef == null) {
			authorityService.createAuthority(AuthorityType.GROUP, name);
			nodeRef = getGroupNode(name);
		}
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(propertyQName.getQName(), subtype);
		nodeService.addAspect(nodeRef, aspectQName.getQName(), properties);
		return AuthorityType.GROUP.getPrefixString() + name;
	}
	
	@Override
	public void deleteTypedGroup(String name) {
		if(isTypedGroup(name)) {
			authorityService.deleteAuthority(getFullName(name));
		}
	}
	
	@Override
	public void convertToSimpleGroup(String name) {
		NodeRef nodeRef = getGroupNode(name);
		if(isTypedGroup(nodeRef)) {
			nodeService.removeAspect(nodeRef, aspectQName.getQName());
		}
	}

	/////////////////////////////////////////////////////////////////
	//                        Private staff                        //
	/////////////////////////////////////////////////////////////////

	private String getFullName(String name) {
		if(name.startsWith(AuthorityType.GROUP.getPrefixString())) {
			return name;
		}
		return AuthorityType.GROUP.getPrefixString() + name;
	}
	
	private NodeRef getGroupNode(String name) {
		return authorityService.getAuthorityNodeRef(getFullName(name));
	}
	
	private boolean isTypedGroup(NodeRef nodeRef) {
		if(nodeRef == null) {
			return false;
		}
		return nodeService.hasAspect(nodeRef, aspectQName.getQName());
	}
	
	private String getGroupSubtype(NodeRef group) {
		String subtype = (String) nodeService.getProperty(group, propertyQName.getQName());
		return subtype;
	}

	@Override
	public List<String> filterTypedGroups(Collection<String> groups, String subtype) {
		List<String> filteredGroups = new ArrayList<String>();
		for(String group : groups) {
			NodeRef groupNodeRef = getGroupNode(group);
			if(this.isTypedGroup(groupNodeRef)) {
				String groupSubtype = getGroupSubtype(groupNodeRef);
				if(subtype == null || subtype.equals(groupSubtype)) {
					filteredGroups.add(group);
				}
			}
		}
		return filteredGroups;
	}

}
