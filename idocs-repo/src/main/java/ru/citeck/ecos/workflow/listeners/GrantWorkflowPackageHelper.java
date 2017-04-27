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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import ru.citeck.ecos.deputy.AuthorityHelper;
import ru.citeck.ecos.model.BpmModel;
import ru.citeck.ecos.security.GrantPermissionService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Grant Workflow Package helper - utility class, that helps to grant/revoke permissions 
 * on workflow package on task or process scope.
 * E.g. when task is started, permissions are granted, when task is ended - permissions are revoked.
 * 
 * @author Sergey Tiunov
 */
public class GrantWorkflowPackageHelper {
	
    private static final String TASK_PROVIDER_PREFIX = "task-activiti-";
    private static final String PROCESS_PROVIDER_PREFIX = "process-activiti-";
	private GrantPermissionService grantPermissionService;
	private AuthorityHelper authorityHelper;

	public void setGrantPermissionService(GrantPermissionService grantPermissionService) {
		this.grantPermissionService = grantPermissionService;
	}

	/**
	 * Grant specified permission to task assignees on a task scope.
	 * 
	 * @param task
	 * @param permission
	 */
	public void grant(DelegateTask task, final String permission) {
		grant(task, permission, false);
	}

	/**
	 * Grant specified permission to task assignees on a task scope.
	 *
	 * @param task
	 * @param permission
	 */
	public void grant(WorkflowTask task, final String permission) {
		grant(task, permission, false);
	}
	
	/**
	 * Grant specified permission to task assignees.
	 * 
	 * @param task
	 * @param permission
	 * @param processScope - true if permission should be set on process scope, false - if on task scope
	 */
	public void grant(DelegateTask task, final String permission, boolean processScope) {
		
		final Set<String> authorities = getTaskActors(task);
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
		final String provider = processScope ? 
				getProcessPermissionProvider(task.getExecution()) : 
				getTaskPermissionProvider(task);

		if(authorities.size() == 0 || workflowPackage == null) return;

		// grant specified permission on workflow package to all task actors:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				
				for(String authority : authorities) {
					grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
				}
				
				return null;
			}
		});

	}


	/**
	 * Grant specified permission to task assignees.
	 *
	 * @param task
	 * @param permission
	 * @param processScope - true if permission should be set on process scope, false - if on task scope
	 */
	public void grant(WorkflowTask task, final String permission, boolean processScope) {

		final Set<String> authorities = getTaskActors(task);
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
		final String provider = getTaskPermissionProvider(task);

		if (authorities.size() == 0 || workflowPackage == null) return;

		// grant specified permission on workflow package to all task actors:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				for (String authority : authorities) {
					grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
				}

				return null;
			}
		});

	}
	
	/**
	 * Grant specified permission to specified authority on process scope.
	 * 
	 * @param execution
	 * @param permission
	 */
	public void grant(DelegateExecution execution, final String authority, final String permission) {
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(execution);
		final String provider = getProcessPermissionProvider(execution);
		
		if(workflowPackage == null) return;

		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				grantPermissionService.grantPermission(workflowPackage, authority, permission, provider);
				return null;
			}
		});
	}
	
	/**
	 * Revoke all permissions, granted on task scope.
	 * 
	 * @param task
	 */
	public void revoke(DelegateTask task) {
		
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
		final String provider = getTaskPermissionProvider(task);

		if(workflowPackage == null) return;

		// revoke all task-granted permissions from workflow package:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				
				grantPermissionService.revokePermission(workflowPackage, provider);

				return null;
			}
		});
	}

	/**
	 * Revoke all permissions, granted on task scope.
	 *
	 * @param task
	 */
	public void revoke(WorkflowTask task) {

		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(task);
		final String provider = getTaskPermissionProvider(task);

		if (workflowPackage == null) return;

		// revoke all task-granted permissions from workflow package:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				grantPermissionService.revokePermission(workflowPackage, provider);

				return null;
			}
		});
	}
	
	/**
	 * Revoke all permissions granted on task scope.
	 * 
	 * @param task
	 * @param execution
	 */
	public void revoke(Task task, DelegateExecution execution) {
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(execution);
		final String provider = getTaskPermissionProvider(task);

		if(workflowPackage == null) return;

		// revoke all task-granted permissions from workflow package:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				
				grantPermissionService.revokePermission(workflowPackage, provider);
				
				return null;
			}
		});
	}
	
	/**
	 * Revoke all permissions granted on process scope.
	 * 
	 * @param execution
	 */
	public void revoke(DelegateExecution execution) {
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(execution);
		final String provider = getProcessPermissionProvider(execution);

		if(workflowPackage == null) return;

		// revoke all process-granted permissions from workflow package:
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				
				grantPermissionService.revokePermission(workflowPackage, provider);
				
				return null;
			}
		});
	}
	
	// get task actors (authorities)
	private Set<String> getTaskActors(DelegateTask task) {
		Set<String> actors = new HashSet<String>();
		// add actor
		String actor = task.getAssignee();
		if(actor != null) {
			actors.add(actor);
		}
		// add pooled actors
		Set<IdentityLink> candidates = ((TaskEntity)task).getCandidates();
		if(candidates != null) {
			for(IdentityLink candidate : candidates) {
				if(candidate.getGroupId() != null) {
					actors.add(candidate.getGroupId());
				}
				if(candidate.getUserId() != null) {
					actors.add(candidate.getUserId());
				}
			}
		}
		return actors;
	}

	// get task actors (authorities)
	private Set<String> getTaskActors(WorkflowTask task) {
		Set<String> actors = new HashSet<String>();
		// add actor
		String actor = (String) task.getProperties().get(ContentModel.PROP_OWNER);
		if (actor != null) {
			actors.add(actor);
		}
		// add pooled actors
		List<NodeRef> candidates = (List<NodeRef>) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
		if (candidates != null) {
			for (NodeRef candidate : candidates) {
				actors.add(authorityHelper.getAuthorityName(candidate));
			}
		}
		return actors;
	}

	// get task permission provider
	private String getTaskPermissionProvider(DelegateTask task) {
		return TASK_PROVIDER_PREFIX + task.getId();
	}

	// get task permission provider
	private String getTaskPermissionProvider(Task task) {
		return TASK_PROVIDER_PREFIX + task.getId();
	}

	// get task permission provider
	private String getTaskPermissionProvider(WorkflowTask task) {
		return TASK_PROVIDER_PREFIX + task.getProperties().get(BpmModel.PROPERTY_TASK_ID);
	}

	// get process permission provider
	private String getProcessPermissionProvider(DelegateExecution execution) {
		return PROCESS_PROVIDER_PREFIX + execution.getId();
	}

	public void setAuthorityHelper(AuthorityHelper authorityHelper) {
		this.authorityHelper = authorityHelper;
	}
}
