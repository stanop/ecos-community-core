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

import java.util.Arrays;
import java.util.Collections;

import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.service.ServiceRegistry;

import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapUsers;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapGroups;

public class DelegateServiceJSImpl extends AlfrescoScopableProcessorExtension 
	implements DelegateServiceGeneric<ScriptGroup[], ScriptUser[], String[]>
{
	
	private DelegateService delegateService;

	@Override
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		super.setServiceRegistry(serviceRegistry);
		this.delegateService = (DelegateService) serviceRegistry.getService(CiteckServices.DELEGATE_SERVICE);
	}
	
	/////////////////////////////////////////////////////////////////
	//                      ADMIN INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public ScriptUser[] getUserDelegates(String userName) {
		return wrapUsers(delegateService.getUserDelegates(userName), this);
	}
	
	@Override
	public void addUserDelegates(String userName, String[] delegates) {
		delegateService.addUserDelegates(userName, Arrays.asList(delegates));
	}

	public void addUserDelegate(String userName, String delegateName) {
		delegateService.addUserDelegates(userName, Collections.singletonList(delegateName));
	}
	
	@Override
	public void removeUserDelegates(String userName, String[] delegates) {
		delegateService.removeUserDelegates(userName, Arrays.asList(delegates));
	}

	@Override
	public ScriptUser[] getUsersWhoHaveThisUserDelegate(String userName) {
		return wrapUsers(delegateService.getUsersWhoHaveThisUserDelegate(userName), this);
	}

	public void removeUserDelegate(String userName, String delegateName) {
		delegateService.removeUserDelegates(userName, Collections.singletonList(delegateName));
	}

	@Override
	public ScriptUser[] getRoleDelegates(String roleFullName) {
		return wrapUsers(delegateService.getRoleDelegates(roleFullName), this);
	}
	
	@Override
	public void addRoleDelegates(String roleFullName, String[] delegates) {
		delegateService.addRoleDelegates(roleFullName, Arrays.asList(delegates));
	}

	public void addRoleDelegate(String roleFullName, String delegateName) {
		delegateService.addRoleDelegates(roleFullName, Collections.singletonList(delegateName));
	}
	
	@Override
	public void removeRoleDelegates(String roleFullName, String[] delegates) {
		delegateService.removeRoleDelegates(roleFullName, Arrays.asList(delegates));
	}

	public void removeRoleDelegate(String roleFullName, String delegateName) {
		delegateService.removeRoleDelegates(roleFullName, Collections.singletonList(delegateName));
	}
	
	@Override
	public boolean isRoleDelegatedByMembers(String roleFullName) {
		return delegateService.isRoleDelegatedByMembers(roleFullName);
	}

	@Override
	public boolean isRoleDelegatedByUser(String roleFullName, String userName) {
		return delegateService.isRoleDelegatedByUser(roleFullName, userName);
	}

	@Override
	public boolean isRoleDelegatedToUser(String roleFullName, String userName) {
		return delegateService.isRoleDelegatedToUser(roleFullName, userName);
	}
	
	@Override
	public ScriptGroup[] getUserRoles(String userName) {
		return wrapGroups(delegateService.getUserRoles(userName), this);
	}

    @Override
    public ScriptGroup[] getUserBranches(String userName) {
        return wrapGroups(delegateService.getUserBranches(userName), this);
    }

    @Override
	public ScriptUser[] getRoleMembers(String roleFullName) {
		return wrapUsers(delegateService.getRoleMembers(roleFullName), this);
	}

	@Override
	public ScriptGroup[] getRolesDelegatedByUser(String userName) {
		return wrapGroups(delegateService.getRolesDelegatedByUser(userName), this);
	}
	
	@Override
	public ScriptGroup[] getRolesDelegatedToUser(String userName) {
		return wrapGroups(delegateService.getRolesDelegatedToUser(userName), this);
	}
	
	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public ScriptUser[] getCurrentUserDelegates() {
		return wrapUsers(delegateService.getCurrentUserDelegates(), this);
	}

	@Override
	public void addCurrentUserDelegates(String[] delegates) {
		delegateService.addCurrentUserDelegates(Arrays.asList(delegates));
	}

	@Override
	public void removeCurrentUserDelegates(String[] delegates) {
		delegateService.removeCurrentUserDelegates(Arrays.asList(delegates));
	}

	@Override
	public boolean isRoleDelegatedByCurrentUser(String roleFullName) {
		return delegateService.isRoleDelegatedByCurrentUser(roleFullName);
	}

	@Override
	public boolean isRoleDelegatedToCurrentUser(String roleFullName) {
		return delegateService.isRoleDelegatedToCurrentUser(roleFullName);
	}

	@Override
	public ScriptGroup[] getCurrentUserRoles() {
		return wrapGroups(delegateService.getCurrentUserRoles(), this);
	}

    @Override
    public ScriptGroup[] getCurrentUserBranches() {
        return wrapGroups(delegateService.getCurrentUserBranches(), this);
    }

    @Override
	public ScriptGroup[] getRolesDelegatedByCurrentUser() {
		return wrapGroups(delegateService.getRolesDelegatedByCurrentUser(), this);
	}

	@Override
	public ScriptGroup[] getRolesDelegatedToCurrentUser() {
		return wrapGroups(delegateService.getRolesDelegatedToCurrentUser(), this);
	}

}
