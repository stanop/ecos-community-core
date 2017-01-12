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

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowEngine;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import ru.citeck.ecos.workflow.activiti.query.QueryExecutor;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskMapper;
import ru.citeck.ecos.workflow.tasks.AdvancedTaskQuery;

import java.util.ArrayList;
import java.util.List;

class AdvancedWorkflowServiceActivitiImpl implements AdvancedWorkflowService {

    private ActivitiWorkflowEngine engine;

    private TaskService taskService;

    private AuthorityService authorityService;

    private QueryExecutor queryExecutor;

    public void setEngine(ActivitiWorkflowEngine engine) {
        this.engine = engine;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setQueryExecutor(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public List<WorkflowTask> queryTasks(AdvancedTaskQuery query) {
        AdvancedTaskQuery activitiQuery = convert(query);
        List<?> tasks = queryExecutor.execute(AdvancedTaskMapper.SELECT_TASKS_CANDIDATE, activitiQuery);
        return convert(tasks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Task> testQueryTasks(AdvancedTaskQuery query) {
        AdvancedTaskQuery activitiQuery = convert(query);
        List tasks = queryExecutor.execute(AdvancedTaskMapper.SELECT_TASKS_CANDIDATE, activitiQuery);
        return (List<Task>) tasks;
    }

    private AdvancedTaskQuery convert(AdvancedTaskQuery query) {
        if (query.getCandidateGroups() != null) {
            List<String> allGroupsList = new ArrayList<String>();
            for (String candidateGroup : query.getCandidateGroups()) {
                allGroupsList.add(candidateGroup);
                allGroupsList.addAll(authorityService.getContainingAuthorities(null, candidateGroup, false));
            }
            query.setCandidateGroups(allGroupsList);
        }
        return query;
    }

    private WorkflowTask convert(Task task) {
        return engine.getTaskById(engine.createGlobalId(task.getId()));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> convert(List<?> inputs) {
        List<T> outputs = new ArrayList<T>(inputs.size());
        for (Object input : inputs) {
            if (input instanceof Task) {
                outputs.add((T) convert((Task) input));
            }
        }
        return outputs;
    }
}
