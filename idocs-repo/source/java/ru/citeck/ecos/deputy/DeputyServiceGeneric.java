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
package ru.citeck.ecos.deputy;

import java.util.List;

/**
 * Deputy Service is a service for managing user deputies and role deputies.
 * It contains basic get/add/remove operations for user deputies and role deputies.
 * 
 * User deputies for user A are other users, that do work for user A, while he is absent (not available).
 * Role deputies for role A are users, that do work for role A, when it is necessary 
 *  (e.g. there are no available members of the role, or not enough of them).
 *  
 * @author Sergey Tiunov
 */
public interface DeputyServiceGeneric<RoleList, UserList, UserNameList> {

	/////////////////////////////////////////////////////////////////
	//                      ADMIN INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Get deputies (users) of specified user.
	 * 
	 * @param userName - user, that is deputied
	 * @return list of users, that are deputies for specified user
	 */
	public abstract UserList getUserDeputies(String userName);

	/**
     * Get assistants (users) of specified user.
     *
     * @param userName - user, that have assistants
     * @return list of users, that are assistants for specified user
     */
    public abstract UserList getUserAssistants(String userName);

    /**
     * Get deputies and assistants (users) of specified user.
     *
     * @param userName - user, that have assistants and deputies
     * @return list of users, that are assistants and deputies for specified user
     */
    public abstract UserList getAllUserDeputies(String userName);

    /**
     * Add specified users to deputies of specified user.
	 * 
	 * @param userName - user, that is deputied
	 * @param deputies - list of users, that deputy
	 */
	public abstract void addUserDeputies(String userName,
			UserNameList deputies);

    /**
     * Add specified users to assistants of specified user.
     *
     * @param userName   - user, that is deputied
     * @param assistants - list of assistant users
     */
    public abstract void addUserAssistants(String userName,
                                           UserNameList assistants);
	/**
	 * Remove specified users from deputies of specified user.
	 * 
	 * @param userName - user, that is deputied
	 * @param deputies - list of users, that deputy
	 */
	public abstract void removeUserDeputies(String userName,
                                            UserNameList deputies);

    /**
     * Remove specified users from assistannts of specified user.
     *
     * @param userName - user, that is deputied
     * @param deputies - list of users, that assistants
     */
    public abstract void removeUserAssistants(String userName,
                                              UserNameList deputies);


	/**
	 * Get users who have specified user as deputy
	 *
	 * @param userName - user who is deputied
	 * @return list of users, who have specified user as deputy
	 */
	public abstract UserList getUsersWhoHaveThisUserDeputy(String userName);

	/**
	 * Get deputies (users) of specified role.
	 * 
	 * @param roleFullName - role, that is deputied
	 * @return list of users, that are deputies for specified role
	 */
	public abstract UserList getRoleDeputies(String roleFullName);

    /**
     * Get assistants (users) of specified role.
     *
     * @param roleFullName - role, that is assistant
     * @return list of users, that are assistants for specified role
     */
    public abstract UserList getRoleAssistants(String roleFullName);

	/**
	 * Add specified users to deputies of specified role.
	 * 
	 * @param roleFullName - role, that is deputied
	 * @param deputies - list of users, that deputy
	 */
	public abstract void addRoleDeputies(String roleFullName,
                                         UserNameList deputies);

    /**
     * Add specified users to assistants of specified role.
     *
     * @param roleFullName - role, that is assistant
     * @param deputies     - list of users, that assistant
     */
    public abstract void addRoleAssistants(String roleFullName,
			UserNameList deputies);

	/**
	 * Remove specified users from deputies of specified role.
	 * 
	 * @param roleFullName - role, that is deputied
	 * @param deputies - list of users, that deputy
	 */
	public abstract void removeRoleDeputies(String roleFullName,
                                            UserNameList deputies);

    /**
     * Remove specified users from assistants of specified role.
     *
     * @param roleFullName - role, that is assistants
     * @param deputies     - list of users, that assistant
     */
    public abstract void removeRoleAssistants(String roleFullName,
			UserNameList deputies);

	/**
	 * Check if role can be deputied by its members.
	 * Note. If this flag is set to true, it means that 
	 *       the full members of the role (non-deputies) 
	 *       can set deputies for this role.
	 *       If this flag is set to false, it means that 
	 *       only system administrator can do it.
	 * 
	 * @param roleFullName - role to check
	 * @return true, if role can be deputied by its members, false otherwise
	 */
	public abstract boolean isRoleDeputiedByMembers(String roleFullName);

	/**
	 * Check if the role can be deputied by specified user.
	 * This means that specified user can set role deputies.
	 * 
	 * @param roleFullName - role full name (e.g. "GROUP_citeck_director")
	 * @param userName - user name (e.g. "admin")
	 * @return - true, if specified role can be deputied by specified user
	 */
	public abstract boolean isRoleDeputiedByUser(String roleFullName,
			String userName);

	/**
	 * Check if the role is deputied to specified user.
	 * 
	 * @param roleFullName
	 * @param userName
	 * @return
     */
    public abstract boolean isRoleDeputiedToUser(String roleFullName,
                                                 String userName);

	public abstract boolean isRoleAssistedToUser(String roleFullName,
                                                 String userName);

	/**
	 * Get own roles of user.
	 * This means the roles, in which he is a full member (i.e. not deputied roles).
	 * 
	 * @param userName
	 * @return - list of user roles, in which he is a full member
	 */
	public abstract RoleList getUserRoles(String userName);

    /**
     * Get own branches of user.
     * This means the branches, in which he is a member
     *
     * @param userName
     * @return - list of user branches, in which he is a member
     */
    public abstract RoleList getUserBranches(String userName);

	/**
	 * Get full members of specified role.
	 * Full member is a member, that is not deputy.
	 * 
	 * @param roleFullName
	 * @return - list of users, that are full members of specified role
	 */
	public abstract UserList getRoleMembers(String roleFullName);

	/**
	 * Get list of roles, that can be deputied by specified user.
	 * This means that specified user can set role deputies.
	 * 
	 * @param userName
	 * @return - list of roles, that can be deputied by user.
	 */
	public abstract RoleList getRolesDeputiedByUser(String userName);

	/**
	 * Get list of roles, that are deputied to specified user.
	 * 
	 * @param userName
	 * @return list of roles, that are deputied to specified user.
	 */
	public abstract RoleList getRolesDeputiedToUser(String userName);

	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////

    /**
     * @see #getUserDeputies(String)
     */
    public abstract UserList getCurrentUserDeputies();

    public abstract UserList getCurrentUserAssistants();

	public abstract UserList getAllCurrentUserDeputies();

	/**
	 * @see #addUserDeputies(String, List)
     */
    public abstract void addCurrentUserDeputies(UserNameList deputies);

    /**
     * @see #addUserAssistants(String, Object)
     */
    public abstract void addCurrentUserAssistants(UserNameList assistants);

    /**
     * Is user is assistant depute
     *
     * @param userName
     * @param assistantUserName
     * @return is user assistant depute
     */
    public abstract boolean isAssistantUserByUser(String userName, String assistantUserName);

	public abstract boolean isAssistantToCurrentUser(String assistantUserName);


    /**
     * @see #removeUserDeputies(String, List)
     */
    public abstract void removeCurrentUserDeputies(UserNameList deputies);

	public abstract void removeCurrentUserAssistants(UserNameList assistance);

	/**
	 * @see #isRoleDeputiedByUser(String, String)
	 */
	public abstract boolean isRoleDeputiedByCurrentUser(String roleFullName);

	/**
	 * @see #isRoleDeputiedToUser(String, String)
	 */
	public abstract boolean isRoleDeputiedToCurrentUser(String roleFullName);

	/**
	 * @see #getUserRoles(String)
	 */
	public abstract RoleList getCurrentUserRoles();

    /**
     * @see #getUserBranches(String)
     */
    public abstract RoleList getCurrentUserBranches();

	/**
	 * @see #getRolesDeputiedByUser(String)
	 */
	public abstract RoleList getRolesDeputiedByCurrentUser();

    /**
     * @see #getRolesDeputiedToUser(String)
     */
    public abstract RoleList getRolesDeputiedToCurrentUser();

    public abstract boolean isUserAvailable(String userName);

	public abstract boolean isCanDeleteDeputeOrAssistantFromRole(String roleFullName);

}