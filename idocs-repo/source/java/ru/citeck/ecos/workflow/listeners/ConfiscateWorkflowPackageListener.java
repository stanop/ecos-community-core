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

/**
 * Confiscates workflow package and its descendants from their owners.
 * Having this done, workflow items have only those permissions, that are set directly on it, 
 * i.e. permissions, set by GrantPermissionService.
 * Should be set on 'start' process event.
 * 
 * @author Sergey Tiunov
 */
public class ConfiscateWorkflowPackageListener extends AbstractExecutionListener {

	private ConfiscateWorkflowPackageHelper helper;
	
	public void setHelper(ConfiscateWorkflowPackageHelper helper) {
		this.helper = helper;
	}
	
	protected void initImpl() {
		setHelper(super.getBean(ConfiscateWorkflowPackageHelper.class));
	}
	
	@Override
	protected void notifyImpl(DelegateExecution execution) throws Exception {
		impl(execution);
	}
	
	private void impl(DelegateExecution execution) {
		helper.confiscatePackage(execution);
	}

}
