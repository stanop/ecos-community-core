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

public interface GrantModel {
	
	public static final String NAMESPACE = "http://www.citeck.ru/model/grant-permission/1.0";
	
	public static final QName TYPE_PERMISSION = QName.createQName(NAMESPACE, "permission");
	public static final QName PROP_PROVIDER = QName.createQName(NAMESPACE, "provider");
	public static final QName PROP_AUTHORITY = QName.createQName(NAMESPACE, "authority");
	public static final QName PROP_PERMISSION = QName.createQName(NAMESPACE, "permission");
	public static final QName PROP_ALLOW = QName.createQName(NAMESPACE, "allow");	
	
	public static final QName ASPECT_GRANTED = QName.createQName(NAMESPACE, "granted");
	public static final QName ASSOC_PERMISSIONS = QName.createQName(NAMESPACE, "permissions");

	public static final QName ASPECT_CONFISCATED = QName.createQName(NAMESPACE, "confiscated");
	public static final QName PROP_OWNER = QName.createQName(NAMESPACE, "owner");
	public static final QName PROP_INHERITS = QName.createQName(NAMESPACE, "inherits");

}
