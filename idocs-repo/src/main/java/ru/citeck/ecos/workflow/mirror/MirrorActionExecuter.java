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

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

public class MirrorActionExecuter extends ActionExecuterAbstractBase 
{
	public static final String NAME = "mirror-task";
	public static final String PARAM_TASK_ID = "taskId";
	private WorkflowMirrorService workflowMirrorService;

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		String taskId = (String) action.getParameterValue(PARAM_TASK_ID);
		if(taskId != null) {
			workflowMirrorService.mirrorTask(taskId);
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_TASK_ID, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_TASK_ID)));
	}

	public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
		this.workflowMirrorService = workflowMirrorService;
	}

}
