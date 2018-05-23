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
 * "Grant workflow package" activiti task listener.
 * Grants workflow package and its children to task assignees and candidate users/groups.
 * Can be set at 'create' or 'assignment' task events.
 *
 * @author Sergey Tiunov
 */
public class GrantWorkflowPackageListener implements TaskListener {

	private static final String VAR_GRANTED_PERMISSION = "grantedPermission";

	private GrantWorkflowPackageHelper helper;
	private String grantedPermission;

	public void setHelper(GrantWorkflowPackageHelper helper) {
		this.helper = helper;
	}

	/**
	 * Set permission to be granted by task listener.
	 * This can be overridden by task variable 'grantedPermission'.
	 * @param grantedPermission
	 */
	public void setGrantedPermission(String grantedPermission) {
		this.grantedPermission = grantedPermission;
	}

	@Override
	public void notify(DelegateTask task) {

		// if it is assignment (not create) - first revoke all given permissions
		// UPD: assignment can be fired before create, so we need to revoke in create too
		helper.revoke(task);

		// grant permissions
		String permission = task.hasVariable(VAR_GRANTED_PERMISSION)
				? (String) task.getVariable(VAR_GRANTED_PERMISSION)
				: this.grantedPermission;
		if(permission != null) {
			helper.grant(task, permission);
		}
	}

}
