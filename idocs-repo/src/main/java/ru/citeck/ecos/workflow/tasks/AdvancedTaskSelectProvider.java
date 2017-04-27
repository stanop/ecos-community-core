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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.ibatis.jdbc.SqlBuilder.*;


/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class AdvancedTaskSelectProvider {

    @SuppressWarnings("unchecked")
    public String getAssignedQuery(Map<String, Object> params) {
        BEGIN();
        SELECT("*");
        FROM("ACT_RU_TASK T");
        if (params.containsKey("candidateGroups")) {
            INNER_JOIN("ACT_RU_IDENTITYLINK I on I.TASK_ID_ = T.ID_");
            WHERE("I.TYPE_ = 'candidate'");
            AND();
            WHERE("I.GROUP_ID_ in " + listToSql((List<String>) params.get("candidateGroups")));
        }
        if (params.containsKey("assignee")) {
            AND();
            WHERE("T.ASSIGNEE_ = '" + params.get("assignee") + "'");
        }
        if (params.containsKey("owners")) {
            AND();
            WHERE("T.OWNER_ in " + listToSql((List<String>) params.get("owners")));
        }
        if (params.containsKey("owner")) {
            AND();
            WHERE("T.OWNER_ = '" + params.get("owner") + "'");
        }
        if (params.containsKey("withoutGroupCandidates")) {
            LEFT_OUTER_JOIN("ACT_RU_IDENTITYLINK I on T.ID_ = I.TASK_ID_");
            AND();
            WHERE("I.GROUP_ID_ IS NULL");
        }
        if (params.containsKey("withoutCandidates")) {
            LEFT_OUTER_JOIN("ACT_RU_IDENTITYLINK I on T.ID_ = I.TASK_ID_");
            AND();
            WHERE("I.GROUP_ID_ IS NULL");
            AND();
            WHERE("I.USER_ID_ IS NULL");
        }
        if (params.containsKey("originalOwner")) {
            INNER_JOIN("ACT_RU_VARIABLE V on V.TASK_ID_ = T.ID_");
            AND();
            WHERE("V.NAME_ = 'taskOriginalOwner'");
            AND();
            WHERE("V.TEXT_ = '" + params.get("originalOwner") + "'");
        }
        if (params.containsKey("candidateUser")) {
            if (!params.containsKey("withoutGroupCandidates")) {
                INNER_JOIN("ACT_RU_IDENTITYLINK I on I.TASK_ID_ = T.ID_");
                AND();
                WHERE("I.GROUP_ID_ IS NULL");
            }
            WHERE("I.TYPE_ = 'candidate'");
            AND();
            WHERE("I.USER_ID_ = '" + params.get("candidateUser") + "'");
        }
        if (params.containsKey("assignees")) {
            AND();
            WHERE("T.ASSIGNEE_ in " + listToSql((List<String>) params.get("assignees")));
        }
        return SQL();
    }

    private String listToSql(List<String> itemsList) {
        StringBuilder sql = new StringBuilder("(");
        for (Iterator<String> iterator = itemsList.iterator(); iterator.hasNext(); ) {
            sql.append("'").append(iterator.next()).append("'");
            if (iterator.hasNext()) {
                sql.append(",");
            } else {
                sql.append(")");
            }
        }
        return sql.toString();
    }
}
