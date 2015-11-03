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

public interface DelegateModel {
	
	public static final String NAMESPACE = "http://www.citeck.ru/model/delegate/1.0";

	public static final QName TYPE_DELEGATION_RECORD = QName.createQName(NAMESPACE, "delegationRecord");
	public static final QName ASSOC_DELEGATED_AUTHORITY = QName.createQName(NAMESPACE, "delegatedAuthority");
	public static final QName ASSOC_DELEGATE = QName.createQName(NAMESPACE, "delegate");

	public static final QName ASPECT_AVAILABILITY = QName.createQName(NAMESPACE, "availability");
	public static final QName PROP_AVAILABLE = QName.createQName(NAMESPACE, "available");
	
	public static final QName ASPECT_ROLE_SETTINGS = QName.createQName(NAMESPACE, "roleSettings");
	public static final QName PROP_MANAGED_BY_MEMBERS = QName.createQName(NAMESPACE, "managedByMembers");

}
