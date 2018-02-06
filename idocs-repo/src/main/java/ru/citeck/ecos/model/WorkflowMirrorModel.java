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

import javax.print.attribute.standard.MediaSize;

public interface WorkflowMirrorModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/workflow-mirror/1.0";

    public static final QName ASPECT_ATTRIBUTES = QName.createQName(NAMESPACE, "attributes");
    public static final QName PROP_TASK_TYPE = QName.createQName(NAMESPACE, "taskType");
    public static final QName PROP_WORKFLOW_ID = QName.createQName(NAMESPACE, "workflowId");
    public static final QName PROP_ACTORS = QName.createQName(NAMESPACE, "actors");
    public static final QName PROP_ASSIGNEE = QName.createQName(NAMESPACE, "assignee");
    public static final QName PROP_DOCUMENT = QName.createQName(NAMESPACE, "document");
    public static final QName PROP_DOCUMENT_TYPE = QName.createQName(NAMESPACE, "documentType");
    public static final QName PROP_DOCUMENT_TYPE_TITLE = QName.createQName(NAMESPACE, "documentTypeTitle");
    public static final QName PROP_DOCUMENT_KIND = QName.createQName(NAMESPACE, "documentKind");
    public static final QName PROP_DOCUMENT_KIND_TITLE = QName.createQName(NAMESPACE, "documentKindTitle");
    public static final QName ASPECT_MIRROR_TASKS = QName.createQName(NAMESPACE, "mirrorTasks");
    public static final QName ASSOC_MIRROR_TASK = QName.createQName(NAMESPACE, "mirrorTask");
    public static final QName PROP_ASSIGNEE_MANAGER = QName.createQName(NAMESPACE, "assigneeManager");
    public static final QName PROP_WORKFLOW_NAME = QName.createQName(NAMESPACE, "workflowName");
    public static final QName PROP_WORKFLOW_INITIATOR = QName.createQName(NAMESPACE, "workflowInitiator");

    public static final QName ASSOC_ASSIGNEE_MIRROR = QName.createQName(NAMESPACE, "assigneeMirror");
}
