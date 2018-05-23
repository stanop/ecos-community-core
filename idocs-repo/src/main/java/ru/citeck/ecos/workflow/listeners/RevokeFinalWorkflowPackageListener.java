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

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.task.Task;

/**
 * "Revoke workflow package" activiti execution listener.
 * Revokes all permissions, that were granted on task scope and on process scope.
 * Should be set on 'end' process event.
 * 
 * @author Sergey Tiunov
 */
public class RevokeFinalWorkflowPackageListener implements ExecutionListener {
	
	private static final String VAR_REVOKE_TASK_PERMISSIONS = "revokeTaskPermissions";
	private static final String VAR_REVOKE_PROCESS_PERMISSIONS = "revokeProcessPermissions";
	
	private GrantWorkflowPackageHelper helper;
    
    private boolean revokeTaskPermissions = true;
    private boolean revokeProcessPermissions = true;

	public void setHelper(GrantWorkflowPackageHelper helper) {
		this.helper = helper;
	}

    public void setRevokeTaskPermissions(boolean revoke) {
        this.revokeTaskPermissions = revoke;
    }

    public void setRevokeProcessPermissions(boolean revoke) {
        this.revokeProcessPermissions = revoke;
    }

	@Override
	public void notify(DelegateExecution execution) throws Exception {

		// revoke all permissions granted on task scope:
		List<Task> tasks = Context.getProcessEngineConfiguration().getTaskService().createTaskQuery().processInstanceId(execution.getProcessInstanceId()).list();

		Object revokeTaskPermissions = execution.getVariable(VAR_REVOKE_TASK_PERMISSIONS);
        if(revokeTaskPermissions == null) {
            revokeTaskPermissions = this.revokeTaskPermissions;
        }
		if(Boolean.TRUE.equals(revokeTaskPermissions)) {
			for(Task task : tasks) {
				helper.revoke(task, execution);
			}
		}

		// revoke all permissions granted on process scope:
		Object revokeProcessPermissions = execution.getVariable(VAR_REVOKE_PROCESS_PERMISSIONS);
        if(revokeProcessPermissions == null) {
            revokeProcessPermissions = this.revokeProcessPermissions;
        }
		if(Boolean.TRUE.equals(revokeProcessPermissions)) {
			helper.revoke(execution);
		}

	}

}
