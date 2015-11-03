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
package ru.citeck.ecos.workflow.tasks;

import org.activiti.engine.impl.TaskQueryImpl;
import ru.citeck.ecos.workflow.activiti.query.QueryParameterConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts TaskQuery to parameters map.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class TaskQueryConverter implements QueryParameterConverter {

    @Override
    public boolean canConvert(Object parameters) {
        return parameters instanceof TaskQueryImpl;
    }

    @Override
    public Map<String, Object> convert(Object parameters) {
        TaskQueryImpl taskQuery = (TaskQueryImpl) parameters;
        Map<String, Object> parametersMap = new HashMap<String, Object>();
        List<String> candidateGroups = taskQuery.getCandidateGroups();
        if (candidateGroups != null && !candidateGroups.isEmpty()) {
            parametersMap.put("candidateGroups", candidateGroups);
        }
        String candidateUser = taskQuery.getCandidateUser();
        if (candidateUser != null && !candidateUser.isEmpty()) {
            parametersMap.put("candidateUser", candidateUser);
        }
        String assignee = taskQuery.getAssignee();
        if (assignee != null && !assignee.isEmpty()) {
            parametersMap.put("assignee", assignee);
        }
        return parametersMap;
    }
}
