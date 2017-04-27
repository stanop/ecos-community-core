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

public class PassportModel {
	public static final String NAMESPACE = "http://www.citeck.ru/model/passport/1.0";
	
	public static final QName TYPE_PASSPORT = QName.createQName(NAMESPACE, "passport");
	public static final QName PROP_SERIES = QName.createQName(NAMESPACE, "series");
	public static final QName PROP_NUMBER = QName.createQName(NAMESPACE, "number");
	public static final QName PROP_ISSUE_DATE = QName.createQName(NAMESPACE, "issueDate");
	public static final QName PROP_ISSUING_AUTHORITY = QName.createQName(NAMESPACE, "issuingAuthority");
	public static final QName PROP_SUBDIVISION_CODE = QName.createQName(NAMESPACE, "subdivisionCode");
	public static final QName PROP_INFO = QName.createQName(NAMESPACE, "info");
	public static final QName ASSOC_PERSON = QName.createQName(NAMESPACE, "person");

}
