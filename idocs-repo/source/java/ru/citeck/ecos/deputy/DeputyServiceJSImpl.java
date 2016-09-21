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

import java.util.Arrays;
import java.util.Collections;

import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.service.ServiceRegistry;

import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapUsers;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapGroups;

public class DeputyServiceJSImpl extends AlfrescoScopableProcessorExtension 
	implements DeputyServiceGeneric<ScriptGroup[], ScriptUser[], String[]>
{
	
	private DeputyService deputyService;

	@Override
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		super.setServiceRegistry(serviceRegistry);
		this.deputyService = (DeputyService) serviceRegistry.getService(CiteckServices.DEPUTY_SERVICE);
	}
	
	/////////////////////////////////////////////////////////////////
	//                      ADMIN INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public ScriptUser[] getUserDeputies(String userName) {
		return wrapUsers(deputyService.getUserDeputies(userName), this);
	}
	
	@Override
	public void addUserDeputies(String userName, String[] deputies) {
		deputyService.addUserDeputies(userName, Arrays.asList(deputies));
	}

	public void addUserDeputy(String userName, String deputyName) {
		deputyService.addUserDeputies(userName, Collections.singletonList(deputyName));
	}
	
	@Override
	public void removeUserDeputies(String userName, String[] deputies) {
		deputyService.removeUserDeputies(userName, Arrays.asList(deputies));
	}

	@Override
	public ScriptUser[] getUsersWhoHaveThisUserDeputy(String userName) {
		return wrapUsers(deputyService.getUsersWhoHaveThisUserDeputy(userName), this);
	}

	public void removeUserDeputy(String userName, String deputyName) {
		deputyService.removeUserDeputies(userName, Collections.singletonList(deputyName));
	}

	@Override
	public ScriptUser[] getRoleDeputies(String roleFullName) {
		return wrapUsers(deputyService.getRoleDeputies(roleFullName), this);
	}
	
	@Override
	public void addRoleDeputies(String roleFullName, String[] deputies) {
		deputyService.addRoleDeputies(roleFullName, Arrays.asList(deputies));
	}

	public void addRoleDeputy(String roleFullName, String deputyName) {
		deputyService.addRoleDeputies(roleFullName, Collections.singletonList(deputyName));
	}
	
	@Override
	public void removeRoleDeputies(String roleFullName, String[] deputies) {
		deputyService.removeRoleDeputies(roleFullName, Arrays.asList(deputies));
	}

	public void removeRoleDeputy(String roleFullName, String deputyName) {
		deputyService.removeRoleDeputies(roleFullName, Collections.singletonList(deputyName));
	}
	
	@Override
	public boolean isRoleDeputiedByMembers(String roleFullName) {
		return deputyService.isRoleDeputiedByMembers(roleFullName);
	}

	@Override
	public boolean isRoleDeputiedByUser(String roleFullName, String userName) {
		return deputyService.isRoleDeputiedByUser(roleFullName, userName);
	}

	@Override
	public boolean isRoleDeputiedToUser(String roleFullName, String userName) {
		return deputyService.isRoleDeputiedToUser(roleFullName, userName);
	}
	
	@Override
	public ScriptGroup[] getUserRoles(String userName) {
		return wrapGroups(deputyService.getUserRoles(userName), this);
	}

    @Override
    public ScriptGroup[] getUserBranches(String userName) {
        return wrapGroups(deputyService.getUserBranches(userName), this);
    }

    @Override
	public ScriptUser[] getRoleMembers(String roleFullName) {
		return wrapUsers(deputyService.getRoleMembers(roleFullName), this);
	}

	@Override
	public ScriptGroup[] getRolesDeputiedByUser(String userName) {
		return wrapGroups(deputyService.getRolesDeputiedByUser(userName), this);
	}
	
	@Override
	public ScriptGroup[] getRolesDeputiedToUser(String userName) {
		return wrapGroups(deputyService.getRolesDeputiedToUser(userName), this);
	}
	
	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public ScriptUser[] getCurrentUserDeputies() {
		return wrapUsers(deputyService.getCurrentUserDeputies(), this);
	}

	@Override
	public void addCurrentUserDeputies(String[] deputies) {
		deputyService.addCurrentUserDeputies(Arrays.asList(deputies));
	}

	@Override
	public void removeCurrentUserDeputies(String[] deputies) {
		deputyService.removeCurrentUserDeputies(Arrays.asList(deputies));
	}

	@Override
	public boolean isRoleDeputiedByCurrentUser(String roleFullName) {
		return deputyService.isRoleDeputiedByCurrentUser(roleFullName);
	}

	@Override
	public boolean isRoleDeputiedToCurrentUser(String roleFullName) {
		return deputyService.isRoleDeputiedToCurrentUser(roleFullName);
	}

	@Override
	public ScriptGroup[] getCurrentUserRoles() {
		return wrapGroups(deputyService.getCurrentUserRoles(), this);
	}

    @Override
    public ScriptGroup[] getCurrentUserBranches() {
        return wrapGroups(deputyService.getCurrentUserBranches(), this);
    }

    @Override
	public ScriptGroup[] getRolesDeputiedByCurrentUser() {
		return wrapGroups(deputyService.getRolesDeputiedByCurrentUser(), this);
	}

	@Override
	public ScriptGroup[] getRolesDeputiedToCurrentUser() {
		return wrapGroups(deputyService.getRolesDeputiedToCurrentUser(), this);
	}

}
