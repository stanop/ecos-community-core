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

public interface BusinessCalendarModel {
	
	public static final String NAMESPACE = "http://www.citeck.ru/model/bcalendar/1.0";
	
	public static final QName TYPE_CALENDAR = QName.createQName(NAMESPACE, "calendar");
	public static final QName PROP_DATE_FROM = QName.createQName(NAMESPACE, "dateFrom");
	public static final QName PROP_DATE_TO = QName.createQName(NAMESPACE, "dateTo");
	public static final QName PROP_REMARK = QName.createQName(NAMESPACE, "remark");

}
