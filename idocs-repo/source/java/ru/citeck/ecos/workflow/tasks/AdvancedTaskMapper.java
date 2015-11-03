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

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public interface AdvancedTaskMapper {

    public static final String SELECT_TASKS_CANDIDATE = "selectAssignedTasks";
    public static final String SELECT_ALL_TASKS = "selectAllTasks";

    @SelectProvider(type = AdvancedTaskSelectProvider.class, method = "getAssignedQuery")
    @Results(value = {
            @Result(property = "id", column = "id_"),
            @Result(property = "revision", column = "rev_"),
            @Result(property = "executionId", column = "execution_id_"),
            @Result(property = "processInstanceId", column = "proc_inst_id_"),
            @Result(property = "processDefinitionId", column = "proc_def_id_"),
            @Result(property = "name", column = "name_"),
            @Result(property = "parentTaskId", column = "parent_task_id_"),
            @Result(property = "descriptionWithoutCascade", column = "description_"),
            @Result(property = "taskDefinitionKeyWithoutCascade", column = "task_def_key_"),
            @Result(property = "ownerWithoutCascade", column = "owner_"),
            @Result(property = "assigneeWithoutCascade", column = "assignee_"),
            @Result(property = "delegationStateString", column = "delegation_"),
            @Result(property = "priorityWithoutCascade", column = "priority_"),
            @Result(property = "createTime", column = "create_time_"),
            @Result(property = "dueDateWithoutCascade", column = "due_date_")})
    public List<TaskEntity> selectAssignedTasks();

    @Select("select * " +
            "from ACT_RU_TASK T ")
    @Results(value = {
            @Result(property = "id", column = "id_"),
            @Result(property = "name", column = "name_"),
            @Result(property = "descriptionWithoutCascade", column = "description_"),
            @Result(property = "ownerWithoutCascade", column = "owner_"),
            @Result(property = "assigneeWithoutCascade", column = "assignee_"),
            @Result(property = "createTime", column = "create_time_"),
            @Result(property = "dueDateWithoutCascade", column = "due_date_")})
    public List<TaskEntity> selectAllTasks();

}
