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
package ru.citeck.ecos.delegate;

import java.util.List;

/**
 * Delegate Service is a service for managing user delegates and role delegates.
 * It contains basic get/add/remove operations for user delegates and role delegates.
 * 
 * User delegates for user A are other users, that do work for user A, while he is absent (not available).
 * Role delegates for role A are users, that do work for role A, when it is necessary 
 *  (e.g. there are no available members of the role, or not enough of them).
 *  
 * @author Sergey Tiunov
 */
public interface DelegateServiceGeneric<RoleList, UserList, UserNameList> {

	/////////////////////////////////////////////////////////////////
	//                      ADMIN INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Get delegates (users) of specified user.
	 * 
	 * @param userName - user, that is delegated
	 * @return list of users, that are delegates for specified user
	 */
	public abstract UserList getUserDelegates(String userName);

	/**
	 * Add specified users to delegates of specified user.
	 * 
	 * @param userName - user, that is delegated
	 * @param delegates - list of users, that delegate
	 */
	public abstract void addUserDelegates(String userName,
			UserNameList delegates);

	/**
	 * Remove specified users from delegates of specified user.
	 * 
	 * @param userName - user, that is delegated
	 * @param delegates - list of users, that delegate
	 */
	public abstract void removeUserDelegates(String userName,
			UserNameList delegates);

	/**
	 * Get users who have specified user as delegate
	 *
	 * @param userName - user who is delegated
	 * @return list of users, who have specified user as delegate
	 */
	public abstract UserList getUsersWhoHaveThisUserDelegate(String userName);

	/**
	 * Get delegates (users) of specified role.
	 * 
	 * @param roleFullName - role, that is delegated
	 * @return list of users, that are delegates for specified role
	 */
	public abstract UserList getRoleDelegates(String roleFullName);

	/**
	 * Add specified users to delegates of specified role.
	 * 
	 * @param roleFullName - role, that is delegated
	 * @param delegates - list of users, that delegate
	 */
	public abstract void addRoleDelegates(String roleFullName,
			UserNameList delegates);

	/**
	 * Remove specified users from delegates of specified role.
	 * 
	 * @param roleFullName - role, that is delegated
	 * @param delegates - list of users, that delegate
	 */
	public abstract void removeRoleDelegates(String roleFullName,
			UserNameList delegates);

	/**
	 * Check if role can be delegated by its members.
	 * Note. If this flag is set to true, it means that 
	 *       the full members of the role (non-delegates) 
	 *       can set delegates for this role.
	 *       If this flag is set to false, it means that 
	 *       only system administrator can do it.
	 * 
	 * @param roleFullName - role to check
	 * @return true, if role can be delegated by its members, false otherwise
	 */
	public abstract boolean isRoleDelegatedByMembers(String roleFullName);

	/**
	 * Check if the role can be delegated by specified user.
	 * This means that specified user can set role delegates.
	 * 
	 * @param roleFullName - role full name (e.g. "GROUP_citeck_director")
	 * @param userName - user name (e.g. "admin")
	 * @return - true, if specified role can be delegated by specified user
	 */
	public abstract boolean isRoleDelegatedByUser(String roleFullName,
			String userName);

	/**
	 * Check if the role is delegated to specified user.
	 * 
	 * @param roleFullName
	 * @param userName
	 * @return
	 */
	public abstract boolean isRoleDelegatedToUser(String roleFullName,
			String userName);

	/**
	 * Get own roles of user.
	 * This means the roles, in which he is a full member (i.e. not delegated roles).
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
	 * Full member is a member, that is not delegate.
	 * 
	 * @param roleFullName
	 * @return - list of users, that are full members of specified role
	 */
	public abstract UserList getRoleMembers(String roleFullName);

	/**
	 * Get list of roles, that can be delegated by specified user.
	 * This means that specified user can set role delegates.
	 * 
	 * @param userName
	 * @return - list of roles, that can be delegated by user.
	 */
	public abstract RoleList getRolesDelegatedByUser(String userName);

	/**
	 * Get list of roles, that are delegated to specified user.
	 * 
	 * @param userName
	 * @return list of roles, that are delegated to specified user.
	 */
	public abstract RoleList getRolesDelegatedToUser(String userName);

	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////
	
	/**
	 * @see #getUserDelegates(String)
	 */
	public abstract UserList getCurrentUserDelegates();

	/**
	 * @see #addUserDelegates(String, List)
	 */
	public abstract void addCurrentUserDelegates(UserNameList delegates);

	/**
	 * @see #removeUserDelegates(String, List)
	 */
	public abstract void removeCurrentUserDelegates(UserNameList delegates);

	/**
	 * @see #isRoleDelegatedByUser(String, String)
	 */
	public abstract boolean isRoleDelegatedByCurrentUser(String roleFullName);

	/**
	 * @see #isRoleDelegatedToUser(String, String)
	 */
	public abstract boolean isRoleDelegatedToCurrentUser(String roleFullName);

	/**
	 * @see #getUserRoles(String)
	 */
	public abstract RoleList getCurrentUserRoles();

    /**
     * @see #getUserBranches(String)
     */
    public abstract RoleList getCurrentUserBranches();

	/**
	 * @see #getRolesDelegatedByUser(String)
	 */
	public abstract RoleList getRolesDelegatedByCurrentUser();

	/**
	 * @see #getRolesDelegatedToUser(String)
	 */
	public abstract RoleList getRolesDelegatedToCurrentUser();

}