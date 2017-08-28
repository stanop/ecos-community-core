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

import ru.citeck.ecos.workflow.activiti.query.QueryParameterConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class AdvancedTaskQueryConverter implements QueryParameterConverter {

    @Override
    public boolean canConvert(Object parameters) {
        return parameters instanceof AdvancedTaskQuery;
    }

    @Override
    public Map<String, Object> convert(Object parameters) {
        AdvancedTaskQuery taskQuery = (AdvancedTaskQuery) parameters;
        Map<String, Object> parametersMap = new HashMap<String, Object>();
        String owner = taskQuery.getOwner();
        if (owner != null && !owner.isEmpty()) {
            parametersMap.put("owner", owner);
        }
        List<String> owners = taskQuery.getOwners();
        if (owners != null && !owners.isEmpty()) {
            parametersMap.put("owners", owners);
        }
        boolean withoutGroupCandidates = taskQuery.isWithoutGroupCandidates();
        if (withoutGroupCandidates) {
            parametersMap.put("withoutGroupCandidates", withoutGroupCandidates);
        }
        boolean withoutCandidates = taskQuery.isWithoutCandidates();
        if (withoutCandidates) {
            parametersMap.put("withoutCandidates", withoutCandidates);
        }
        String originalOwner = taskQuery.getOriginalOwner();
        if (originalOwner != null && !originalOwner.isEmpty()) {
            parametersMap.put("originalOwner", originalOwner);
        }
        List<String> assignees = taskQuery.getAssignees();
        if (assignees != null && !assignees.isEmpty()) {
            parametersMap.put("assignees", assignees);
        }
        String claimOwner = taskQuery.getClaimOwner();
        if (claimOwner != null && !claimOwner.isEmpty()) {
            parametersMap.put("claimOwner", claimOwner);
        }
        return parametersMap;
    }
}
