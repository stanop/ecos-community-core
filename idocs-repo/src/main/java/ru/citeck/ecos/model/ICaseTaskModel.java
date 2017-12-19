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
    public static final String ACTIVITY_NAMESPACE = "http://www.citeck.ru/model/activity/1.0";

    // types
    public static final QName TYPE_TASK = QName.createQName(NAMESPACE, "task");
    public static final QName TYPE_DEFAULT_TASK = QName.createQName(NAMESPACE, "defaultTask");

    // aspects

    // properties
    public static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(NAMESPACE, "workflowDefinitionName");
    public static final QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NAMESPACE, "workflowInstanceId");
    public static final QName PROP_WORKFLOW_VARIABLE_NAME = QName.createQName(NAMESPACE, "workflowVariableName");
    public static final QName PROP_DEADLINE = QName.createQName(NAMESPACE, "dueDate");
    public static final QName PROP_PRIORITY = QName.createQName(NAMESPACE, "priority");
    public static final QName PROP_EXPECTED_PERFORM_TIME = QName.createQName(ACTIVITY_NAMESPACE, "expectedPerformTime");

    // association
    public static final QName ASSOC_ROLE = QName.createQName(NAMESPACE, "role");
    public static final QName ASSOC_TASK_ROLES_MAPPING = QName.createQName(NAMESPACE, "taskRolesMapping");
    public static final QName ASSOC_TASKS = QName.createQName(NAMESPACE, "tasks");
    public static final QName ASSOC_PERFORMER = QName.createQName(NAMESPACE, "performer");
    public static final QName ASSOC_PERFORMERS = QName.createQName(NAMESPACE, "performers");
    public static final QName ASSOC_CONTROLLER = QName.createQName(NAMESPACE, "controller");
    public static final QName ASSOC_WORKFLOW_PACKAGE = QName.createQName(NAMESPACE, "workflowPackage");
    public static final QName ASSOC_CONFIRMERS = QName.createQName(NAMESPACE, "confirmers");

    // constraint
}
