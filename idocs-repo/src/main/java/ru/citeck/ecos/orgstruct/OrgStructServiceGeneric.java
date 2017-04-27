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

/**
 * Generic interface for OrgStructService.
 * 
 * @author Sergey Tiunov
 *
 * @param <GroupType> - type that represent group
 * @param <GroupList> - type that represent list of groups
 */
public interface OrgStructServiceGeneric<GroupType, GroupList> {

	/**
	 * Get type-name of typed-group.
	 * @param name - group name
	 * @return group type name or null, if group is not typed
	 */
	String getGroupType(String name);
	
	/**
	 * Get subtype-name of typed-group.
	 * @param name - group name
	 * @return group subtype name or null, if group is not typed
	 */
	String getGroupSubtype(String name);

	/**
	 * Determines whether specified group has specified type.
	 * @param type - group type
	 * @param name - group name
	 * @return true if group 'name' is of type 'type', false otherwise
	 */
	boolean isTypedGroup(String type, String name);
	
	/**
	 * Returns list of groups of type 'type'.
	 * @param type - group type
	 * @param rootOnly - true to return only root groups, false to return all groups
	 * @return - list or groups, satisfying criteria
	 */
	GroupList getAllTypedGroups(String type, boolean rootOnly);

	/**
	 * Returns list of groups of type 'type' and sub-type 'subtype'.
	 * @param type - group type
	 * @param subtype - group sub-type
	 * @param rootOnly - true to return only root groups, false to return all groups
	 * @return - list or groups, satisfying criteria
	 */
	GroupList getAllTypedGroups(String type, String subtype, boolean rootOnly);

	/**
	 * Create typed group
	 * @param type - group type
	 * @param subtype - group sub-type
	 * @param name - group name
	 * @return - group instance
	 */
	GroupType createTypedGroup(String type, String subtype, String name);

	/**
	 * Delete typed group.
	 * 
	 * @param type - group type
	 * @param name - group name
	 */
	void deleteTypedGroup(String type, String name);

	/**
	 * Convert typed group to simple (non-typed) group.
	 * 
	 * @param name - group name.
	 */
	void convertToSimpleGroup(String name);

	/**
	 * Get list of groups of type 'type', where user 'userName' is a member.
	 * 
	 * @param userName user name
	 * @param type group type
	 * @return list of groups of specified type, that specified user belongs to
	 */
	GroupList getTypedGroupsForUser(String userName, String type);

	/**
	 * Get list of groups of type 'type' and subtype 'subtype', where user 'userName' is a member.
	 * @param userName user name
	 * @param type group type
	 * @param subtype group subtype
	 * @return list of groups of specified type/subtype, that specified user belongs to
	 */
	GroupList getTypedGroupsForUser(String userName, String type, String subtype);
	
	/**
	 * Get manager role for specified branch.
	 * 
	 * @param branchName - branch name
	 * @return group that is manager for specified branch, or null, if there is no manager for this branch
	 */
	GroupType getBranchManager(String branchName);
	
	/**
	 * Get manager role for specified user.
	 * 
	 * @param userName - user name
	 * @return group, that is manager for specified user, or null, if there is no manager for this user
	 */
	GroupType getUserManager(String userName);
	
    /**
     * Get first group of type 'type', which is subgroup of group 'groupName'.
     * 
     * @param groupName parent group name
     * @param type group type
     * @param immediate specify 'true' for only immediate subgroups, or 'false' for all subgroups
     * @return group of specified type, that is subgroup of specified group, or null
     */
	GroupType getTypedSubgroup(String groupName, String type, boolean immediate);

    /**
     * Get list of groups of type 'type', which are subgroups of group 'groupName'.
     * 
     * @param groupName parent group name
     * @param type group type
     * @param immediate specify 'true' for only immediate subgroups, or 'false' for all subgroups
     * @return list of groups of specified type, that are subgroups of specified group
     */
    GroupList getTypedSubgroups(String groupName, String type, boolean immediate);

    /**
     * Get first group of type 'type' and subtype 'subtype', which is subgroup of group 'groupName'.
     * 
     * @param groupName parent group name
     * @param type group type
     * @param subtype group subtype
     * @param immediate specify 'true' for only immediate subgroups, or 'false' for all subgroups
     * @return group of specified type/subtype, that is subgroup of specified group, or null
     */
    GroupType getTypedSubgroup(String groupName, String type, String subtype, boolean immediate);
    
    /**
     * Get list of groups of type 'type' and subtype 'subtype', which are subgroups of group 'groupName'.
     * 
     * @param groupName parent group name
     * @param type group type
     * @param subtype group subtype
     * @param immediate specify 'true' for only immediate subgroups, or 'false' for all subgroups
     * @return list of groups of specified type/subtype, that are subgroups of specified group
     */
    GroupList getTypedSubgroups(String groupName, String type, String subtype, boolean immediate);
    
}
