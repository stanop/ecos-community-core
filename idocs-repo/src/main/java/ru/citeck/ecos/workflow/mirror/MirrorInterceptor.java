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
package ru.citeck.ecos.workflow.mirror;

import java.lang.reflect.Method;

import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MirrorInterceptor implements MethodInterceptor, ApplicationContextAware
{
	private ApplicationContext applicationContext;
	private WorkflowMirrorService mirrorService;
	private String workflowMirrorServiceName;
	
	public void init() {
		this.mirrorService = applicationContext.getBean(workflowMirrorServiceName, WorkflowMirrorService.class);
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable 
	{
		Method method = invocation.getMethod();
		String methodName = method.getName();
		Object result = invocation.proceed();
		
		if(methodName.equals("updateTask")) {
			processUpdateTask(invocation, result);
		}
		
		return result;
	}
	
	private void processUpdateTask(MethodInvocation invocation, Object result) {
		if(result instanceof WorkflowTask) {
            mirrorService.mirrorTask((WorkflowTask) result);
		}
	}

	public void setWorkflowMirrorService(WorkflowMirrorService mirrorService) {
		this.mirrorService = mirrorService;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setWorkflowMirrorServiceName(String workflowMirrorServiceName) {
		this.workflowMirrorServiceName = workflowMirrorServiceName;
	}
	
}
