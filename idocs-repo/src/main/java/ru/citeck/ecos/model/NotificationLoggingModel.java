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

public interface NotificationLoggingModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/notification-logging/1.0";

    public static final QName TYPE_NOTIFICATION_LOG_ITEM = QName.createQName(NAMESPACE, "notificationLogItem");
    public static final QName PROP_NOTIFICATION_DATE = QName.createQName(NAMESPACE, "notificationDate");
    public static final QName PROP_EVENT_TYPE = QName.createQName(NAMESPACE, "eventType");
    public static final QName PROP_IS_NOTIFICATION_SENT = QName.createQName(NAMESPACE, "isNotificationSent");
    public static final QName PROP_NOTIFICATION_RECIPIENT = QName.createQName(NAMESPACE, "notificationRecipient");
    public static final QName PROP_NOTIFICATION_DOCUMENT = QName.createQName(NAMESPACE, "notificationDocument");
    public static final QName PROP_NOTIFICATION_TASK = QName.createQName(NAMESPACE, "notificationTask");
    public static final QName PROP_NOTIFICATION_EMAIL = QName.createQName(NAMESPACE, "notificationEmail");
    public static final QName PROP_NOTIFICATION_SUBJECT = QName.createQName(NAMESPACE, "notificationSubject");
    public static final QName PROP_NOTIFICATION_WOKFLOW_ID = QName.createQName(NAMESPACE, "wokflowId");
	public static enum EventType {
		NotificationForTask, Other
	}

}
