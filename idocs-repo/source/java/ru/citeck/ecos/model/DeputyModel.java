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

public interface DeputyModel {
	
	String NAMESPACE = "http://www.citeck.ru/model/deputy/1.0";

	QName TYPE_DEPUTATION_RECORD 	= QName.createQName(NAMESPACE, "deputationRecord");
	QName ASSOC_DEPUTIED_AUTHORITY 	= QName.createQName(NAMESPACE, "deputiedAuthority");
	QName ASSOC_DEPUTY 				= QName.createQName(NAMESPACE, "deputy");
    QName PROP_IS_ASSISTANT = QName.createQName(NAMESPACE, "isAssistant");

	QName ASPECT_AVAILABILITY 		= QName.createQName(NAMESPACE, "availability");
	QName PROP_AVAILABLE 			= QName.createQName(NAMESPACE, "available");
	
	QName ASPECT_ROLE_SETTINGS 		= QName.createQName(NAMESPACE, "roleSettings");
	QName PROP_MANAGED_BY_MEMBERS 	= QName.createQName(NAMESPACE, "managedByMembers");

	QName TYPE_DEPUTY_ABSENCE_EVENT = QName.createQName(NAMESPACE, "absenceEvent");
	QName PROP_START_ABSENCE 		= QName.createQName(NAMESPACE, "startAbsence");
	QName PROP_END_ABSENCE 			= QName.createQName(NAMESPACE, "endAbsence");
	QName PROP_COMMENT 				= QName.createQName(NAMESPACE, "comment");
	QName ASSOC_USER 				= QName.createQName(NAMESPACE, "user");
	QName ASSOC_REASON_OF_ABSENCE	= QName.createQName(NAMESPACE, "reasonOfAbsence");
	QName PROP_EVENT_FINISHED	= QName.createQName(NAMESPACE, "eventFinished");


}
