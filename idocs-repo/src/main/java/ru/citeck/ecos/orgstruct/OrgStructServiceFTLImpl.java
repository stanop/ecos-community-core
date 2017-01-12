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

import org.alfresco.repo.template.TemplateNode;

import ru.citeck.ecos.utils.AlfrescoTemplateProcessorExtension;
import static ru.citeck.ecos.utils.FreeMarkerImplUtils.wrapGroup;
import static ru.citeck.ecos.utils.FreeMarkerImplUtils.wrapGroups;

/**
 * OrgStructService JavaScript API.
 * Wraps all requests to OrgStructService Java API.
 * 
 * @author Sergey Tiunov
 *
 */
public class OrgStructServiceFTLImpl extends AlfrescoTemplateProcessorExtension implements OrgStructServiceFTL
{
	private OrgStructService orgStructService;
	
	public void setOrgStructService(OrgStructService orgStructService) {
		this.orgStructService = orgStructService;
	}

	@Override
	public String getGroupType(String name) {
		return orgStructService.getGroupType(name);
	}

	@Override
	public boolean isTypedGroup(String type, String name) {
		return orgStructService.isTypedGroup(type, name);
	}

	@Override
	public String getGroupSubtype(String name) {
		return orgStructService.getGroupSubtype(name);
	}

	@Override
	public List<TemplateNode> getAllTypedGroups(String type, boolean rootOnly) {
		return wrapGroups(orgStructService.getAllTypedGroups(type, rootOnly), this);
	}

	@Override
	public List<TemplateNode> getAllTypedGroups(String type, String subtype, boolean rootOnly) {
		return wrapGroups(orgStructService.getAllTypedGroups(type, subtype, rootOnly), this);
	}

	@Override
	public TemplateNode createTypedGroup(String type, String subtype, String name) {
		return wrapGroup(orgStructService.createTypedGroup(type, subtype, name), this);
	}

	@Override
	public void deleteTypedGroup(String type, String name) {
		orgStructService.deleteTypedGroup(type, name);
	}

	@Override
	public void convertToSimpleGroup(String name) {
		orgStructService.convertToSimpleGroup(name);
	}

	@Override
	public List<String> getGroupTypes() {
		return orgStructService.getGroupTypes();
	}

	@Override
	public List<TemplateNode> getTypedGroupsForUser(String userName, String type) {
		return wrapGroups(orgStructService.getTypedGroupsForUser(userName, type), this);
	}

	@Override
	public List<TemplateNode> getTypedGroupsForUser(String userName, String type, String subtype) {
		return wrapGroups(orgStructService.getTypedGroupsForUser(userName, type, subtype), this);
	}

	@Override
	public TemplateNode getBranchManager(String branchName) {
		return wrapGroup(orgStructService.getBranchManager(branchName), this);
	}

	@Override
	public TemplateNode getUserManager(String userName) {
		return wrapGroup(orgStructService.getUserManager(userName), this);
	}

    @Override
    public List<TemplateNode> getTypedSubgroups(String groupName, String type, boolean immediate) {
        return wrapGroups(orgStructService.getTypedSubgroups(groupName, type, immediate), this);
    }

    @Override
    public List<TemplateNode> getTypedSubgroups(String groupName, String type, String subtype, boolean immediate) {
        return wrapGroups(orgStructService.getTypedSubgroups(groupName, type, subtype, immediate), this);
    }

    @Override
    public TemplateNode getTypedSubgroup(String groupName, String type,
            boolean immediate) {
        return wrapGroup(orgStructService.getTypedSubgroup(groupName, type, immediate), this);
    }

    @Override
    public TemplateNode getTypedSubgroup(String groupName, String type,
            String subtype, boolean immediate) {
        return wrapGroup(orgStructService.getTypedSubgroup(groupName, type, subtype, immediate), this);
    }

}
