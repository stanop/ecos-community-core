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
package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * Created by maxim.strizhov on 12.10.2015.
 */
public class ICaseTaskModel {
    // model
    public static final String PREFIX = "icaseTask";

    // namespace
    public static final String NAMESPACE = "http://www.citeck.ru/model/icaseTask/1.0";

    // types
    public static final QName TYPE_TASK = QName.createQName(NAMESPACE, "task");

    // aspects
    public static final QName ASPECT_HAS_TASKS = QName.createQName(NAMESPACE, "hasTasks");

    // properties
    public static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(NAMESPACE, "workflowDefinitionName");
    public static final QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NAMESPACE, "workflowInstanceId");
    public static final QName PROP_WORKFLOW_VARIABLE_NAME = QName.createQName(NAMESPACE, "workflowVariableName");
    public static final QName PROP_DEADLINE = QName.createQName(NAMESPACE, "dueDate");
    public static final QName PROP_PRIORITY = QName.createQName(NAMESPACE, "priority");

    // association
    public static final QName ASSOC_ROLE = QName.createQName(NAMESPACE, "role");
    public static final QName ASSOC_TASK_ROLES_MAPPING = QName.createQName(NAMESPACE, "taskRolesMapping");
    public static final QName ASSOC_TASKS = QName.createQName(NAMESPACE, "tasks");
    public static final QName ASSOC_PERFORMER = QName.createQName(NAMESPACE, "performer");
    public static final QName ASSOC_WORKFLOW_PACKAGE = QName.createQName(NAMESPACE, "workflowPackage");

    // constraint
}
