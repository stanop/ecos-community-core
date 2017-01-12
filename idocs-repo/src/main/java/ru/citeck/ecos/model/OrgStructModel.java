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

public interface OrgStructModel {
	
	public static final String NAMESPACE = "http://www.citeck.ru/model/orgstruct/1.0";

	public static final QName ASPECT_BRANCH_TYPE = QName.createQName(NAMESPACE, "branchType");

	public static final QName ASPECT_ROLE_TYPE = QName.createQName(NAMESPACE, "roleType");
	
	public static final QName ASPECT_BRANCH = QName.createQName(NAMESPACE, "branch");
	public static final QName PROP_BRANCH_TYPE = QName.createQName(NAMESPACE, "branchType");
	public static final QName PROP_BRANCH_INDEX = QName.createQName(NAMESPACE, "branchIndex");

	public static final QName ASPECT_ROLE = QName.createQName(NAMESPACE, "role");	
	public static final QName PROP_ROLE_TYPE = QName.createQName(NAMESPACE, "roleType");
	public static final QName PROP_ROLE_IS_MANAGER = QName.createQName(NAMESPACE, "roleIsManager");

	public static final QName ASPECT_CUSTOM_FIELDS = QName.createQName(NAMESPACE, "customFields");
	public static final QName PROP_CUSTOM_ASPECT = QName.createQName(NAMESPACE, "customAspect");

	public static final QName PROP_FIRST_NAME = QName.createQName(NAMESPACE, "firstName");
	public static final QName PROP_LAST_NAME = QName.createQName(NAMESPACE, "lastName");

}
