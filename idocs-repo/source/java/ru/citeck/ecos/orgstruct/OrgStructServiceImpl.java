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
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

import ru.citeck.ecos.model.OrgStructModel;

/**
 * OrgStructService implementation.
 * Contains registry of TypedGroupDAO.
 * Delegates all requests to corresponding DAO.
 * 
 * @author Sergey Tiunov
 *
 */
public class OrgStructServiceImpl implements OrgStructService
{
	private Map<String, TypedGroupDAO> components;
	private AuthorityService authorityService;
	private NodeService nodeService;
	private OrgMetaService orgMetaService;

	private static final String BRANCH_TYPE = "branch";
	private static final String ROLE_TYPE = "role";
	
	private TypedGroupDAO getComponent(String type) {
		if(!components.containsKey(type)) {
			throw new IllegalArgumentException("No such group type: " + type);
		}
		return components.get(type);
	}

	public void setComponents(Map<String, TypedGroupDAO> components) {
		this.components = components;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setOrgMetaService(OrgMetaService orgMetaService) {
		this.orgMetaService = orgMetaService;
	}

	@Override
	public String getGroupType(String name) {
		for(String type : components.keySet()) {
			if(components.get(type).isTypedGroup(name)) {
				return type;
			}
		}
		return null;
	}
	
	@Override
	public String getGroupSubtype(String name) {
		String type = getGroupType(name);
		if(type == null) {
			return null;
		}
		TypedGroupDAO dao = getComponent(type);
		return dao.getGroupSubtype(name);
	}


	@Override
	public boolean isTypedGroup(String type, String name) {
		return getComponent(type).isTypedGroup(name);
	}

	@Override
	public List<String> getAllTypedGroups(String type, boolean rootOnly) {
		Set<String> groups = getAllGroups(rootOnly);
		return getComponent(type).filterTypedGroups(groups, null);
	}

	@Override
	public List<String> getAllTypedGroups(String type, String subtype, boolean rootOnly) {
		Set<String> groups = getAllGroups(rootOnly);
		return getComponent(type).filterTypedGroups(groups, subtype);
	}

	@Override
	public String createTypedGroup(String type, String subtype, String name) {
		return getComponent(type).createTypedGroup(subtype, name);
	}

	@Override
	public void deleteTypedGroup(String type, String name) {
		getComponent(type).deleteTypedGroup(name);
	}

	@Override
	public void convertToSimpleGroup(String name) {
		for(TypedGroupDAO dao : components.values()) {
			dao.convertToSimpleGroup(name);
		}
	}

	@Override
	public List<String> getGroupTypes() {
		List<String> groupTypes = new ArrayList<String>();
		groupTypes.addAll(components.keySet());
		return groupTypes;
	}

	@Override
	public List<String> getTypedGroupsForUser(String userName, String type) {
		Set<String> userGroups = authorityService.getAuthoritiesForUser(userName);
		return getComponent(type).filterTypedGroups(userGroups, null);
	}
	
	@Override
	public List<String> getTypedGroupsForUser(String userName, String type, String subtype) {
		Set<String> userGroups = authorityService.getAuthoritiesForUser(userName);
		return getComponent(type).filterTypedGroups(userGroups, subtype);
	}

	private Set<String> getAllGroups(boolean rootOnly) {
		if(rootOnly) {
			return authorityService.getAllRootAuthorities(AuthorityType.GROUP);
		} else {
			return authorityService.getAllAuthorities(AuthorityType.GROUP);
		}
	}

	@Override
	public String getBranchManager(String branchName) {
		if(!branchName.startsWith(AuthorityType.GROUP.getPrefixString())) {
			branchName = AuthorityType.GROUP.getPrefixString() + branchName;
		}
		// look for immediate groups inside branch:
		Set<String> immediateGroups = authorityService.getContainedAuthorities(AuthorityType.GROUP, branchName, true);
		for(String group : immediateGroups) {
			if(!isTypedGroup(ROLE_TYPE, group)) {
				continue;
			}
			String subtype = getGroupSubtype(group);
			if(subtype == null) {
				continue;
			}
			NodeRef subtypeNode = orgMetaService.getSubType(ROLE_TYPE, subtype);
			if(subtypeNode == null || !nodeService.exists(subtypeNode)) {
				continue;
			}
			Boolean isManager = (Boolean) nodeService.getProperty(subtypeNode, OrgStructModel.PROP_ROLE_IS_MANAGER);
			if(Boolean.TRUE.equals(isManager)) {
				return group;
			}
		}
		return null;
	}
	
	
	@Override
	public String getUserManager(String userName) {

		// look for the branches "breadth first"
		
		Set<String> groupsToVisit = authorityService.getContainingAuthorities(null, userName, true);
		Set<String> visitedGroups = new TreeSet<String>();

		while(groupsToVisit.size() > 0) {
			Set<String> currentGroups = new TreeSet<String>();
			currentGroups.addAll(groupsToVisit);
			for(String group : currentGroups) {
				if(isTypedGroup(BRANCH_TYPE, group)) {
					String managerGroup = getBranchManager(group);
					if(managerGroup != null) {
						
						Set<String> managerUsers = authorityService.getContainedAuthorities(AuthorityType.USER, managerGroup, false);
						if(!managerUsers.contains(userName)) {
							return managerGroup;
						}
					}
				}
				
				Set<String> parentGroups = authorityService.getContainingAuthorities(null, group, true);
				for(String parentGroup : parentGroups) {
					if(!visitedGroups.contains(parentGroup)) {
						groupsToVisit.add(parentGroup);
					}
				}
				visitedGroups.add(group);
				groupsToVisit.remove(group);
			}
		}		
		return null;
	}

    @Override
    public List<String> getTypedSubgroups(String groupName, String type,
            boolean immediate) {
        Set<String> subgroups = authorityService.getContainedAuthorities(AuthorityType.GROUP, groupName, immediate);
        return getComponent(type).filterTypedGroups(subgroups, null);
    }

    @Override
    public List<String> getTypedSubgroups(String groupName, String type,
            String subtype, boolean immediate) {
        Set<String> subgroups = authorityService.getContainedAuthorities(AuthorityType.GROUP, groupName, immediate);
        return getComponent(type).filterTypedGroups(subgroups, subtype);
    }

    @Override
    public String getTypedSubgroup(String groupName, String type, boolean immediate) {
        List<String> subgroups = this.getTypedSubgroups(groupName, type, immediate);
        if(subgroups.isEmpty()) {
            return null;
        } else {
            return subgroups.iterator().next();
        }
    }

    @Override
    public String getTypedSubgroup(String groupName, String type,
            String subtype, boolean immediate) {
        List<String> subgroups = this.getTypedSubgroups(groupName, type, subtype, immediate);
        if(subgroups.isEmpty()) {
            return null;
        } else {
            return subgroups.iterator().next();
        }
    }

}
