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
package ru.citeck.ecos.workflow;

import java.util.List;

import org.activiti.engine.task.Task;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskQuery;

public interface AdvancedWorkflowService {

	public List<WorkflowTask> queryTasks(AdvancedTaskQuery query);

	public List<Task> testQueryTasks(AdvancedTaskQuery query);

}
