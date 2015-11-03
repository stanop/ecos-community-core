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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

import ru.citeck.ecos.security.ConfiscateService;

/**
 * Utility class, that helps confiscate workflow package and all descendants from their owners - at the start of workflow,
 * and return them to their owners - at the end of workflow.
 * As a secondary feature, it grants Consumer permission to workflow initiator at the start of workflow,
 * because otherwise he/she would have no permissions on workflow items.
 * 
 * @author Sergey Tiunov
 */
public class ConfiscateWorkflowPackageHelper {
	
	private ConfiscateService confiscateService;
	private GrantWorkflowPackageHelper grantHelper;
	
	public void setConfiscateService(ConfiscateService confiscateService) {
		this.confiscateService = confiscateService;
	}
	
	public void setGrantHelper(GrantWorkflowPackageHelper grantHelper) {
		this.grantHelper = grantHelper;
	}

	/**
     * Start-process listener implementation.
     * - confiscate workflow package and its descendants from their owners
     * - grant Consumer to workflow initiator
     * 
     * @param execution
     */
	public void confiscatePackage(final DelegateExecution execution) {
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(execution);
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				confiscateService.confiscateNode(workflowPackage);
				grantHelper.grant(execution, ListenerUtils.getInitiator(execution), PermissionService.CONSUMER);
				return null;
			}
			
		});
	}

	/**
	 * End-process listener implementation.
	 * - return workflow package and its descendants from their owners
	 *
	 * @param execution
	 */
	public void returnPackage(final DelegateExecution execution) {
		final NodeRef workflowPackage = ListenerUtils.getWorkflowPackage(execution);
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				confiscateService.returnNode(workflowPackage);
				return null;
			}
			
		});
	}
	
}
