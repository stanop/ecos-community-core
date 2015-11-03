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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * "Revoke workflow package" activiti task listener.
 * Revokes workflow package from task assignees and candidate users/groups.
 * Optionally can grant other (usually weaker) permission after revoke (postRevokePermission).
 * Should be set on 'complete' task event.
 * 
 * @author Sergey Tiunov
 */
public class RevokeWorkflowPackageListener implements TaskListener {
	
	private static final String VAR_POST_REVOKE_PERMISSION = "postRevokePermission";
	
	private GrantWorkflowPackageHelper helper;
	private String postRevokePermission;
	
	public void setHelper(GrantWorkflowPackageHelper helper) {
		this.helper = helper;
	}

	/**
	 * Set post-revoke permission.
	 * This is the permission to be granted after the revoke.
	 * It can be overridden by task variable 'postRevokePermission'.
	 * @param postRevokePermission
	 */
	public void setPostRevokePermission(String postRevokePermission) {
		this.postRevokePermission = postRevokePermission;
	}

	@Override
	public void notify(DelegateTask task) {
		
		// revoke permission
		helper.revoke(task);
		
		// process post-revoke permission
		String permission = task.hasVariable(VAR_POST_REVOKE_PERMISSION) 
				? (String) task.getVariable(VAR_POST_REVOKE_PERMISSION)
				: this.postRevokePermission;
		if(permission != null && !permission.isEmpty()) {
			helper.grant(task, permission, true);
		}
	}

}
