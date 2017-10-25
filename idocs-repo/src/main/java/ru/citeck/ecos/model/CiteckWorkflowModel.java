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

public interface CiteckWorkflowModel {

    public static final String PREFIX = "cwf";
    public static final String NAMESPACE = "http://www.citeck.ru/model/workflow/1.1";

    public static final QName ASPECT_SENDER = QName.createQName(NAMESPACE, "sender");
    public static final QName ASPECT_ATTACHED_DOCUMENT = QName.createQName(NAMESPACE, "attachedDocument");
    public static final QName ASPECT_ORIGINAL_OWNER = QName.createQName(NAMESPACE, "originalOwner");
    public static final QName ASPECT_URGENCY_CAUSE = QName.createQName(NAMESPACE, "urgencyCause");
    public static final QName ASPECT_PRIORITY = QName.createQName(NAMESPACE, "priority");

    public static final QName PROP_SENDER = QName.createQName(NAMESPACE, "sender");
    public static final QName PROP_SENDER_NAME = QName.createQName(NAMESPACE, "senderName");
    public static final QName PROP_ATTACHED_DOCUMENT = QName.createQName(NAMESPACE, "attachedDocument");
    public static final QName PROP_TASK_ORIGINAL_OWNER = QName.createQName(NAMESPACE, "taskOriginalOwner");
    public static final QName PROP_URGENCY_CAUSE = QName.createQName(NAMESPACE, "urgencyCause");
    public static final QName PROP_PRIORITY = QName.createQName(NAMESPACE, "priority");
    public static final QName PROP_LASTCOMMENT = QName.createQName(NAMESPACE, "lastcomment");
    public static final QName PROP_LAST_TASK_OWNER = QName.createQName(NAMESPACE, "lastTaskOwner");
    public static final QName PROP_IS_OPTIONAL_TASK = QName.createQName(NAMESPACE, "isOptionalTask");
    public static final QName PROP_IS_WORKFLOW_ACTIVE = QName.createQName(NAMESPACE, "isWorkflowActive");
    public static final QName PROP_IS_TASK_CLAIMABLE = QName.createQName(NAMESPACE, "isTaskClaimable");
    public static final QName PROP_IS_TASK_RELEASABLE = QName.createQName(NAMESPACE, "isTaskReleasable");
    public static final QName PROP_IS_TASK_REASSIGNABLE = QName.createQName(NAMESPACE, "isTaskReassignable");


}
