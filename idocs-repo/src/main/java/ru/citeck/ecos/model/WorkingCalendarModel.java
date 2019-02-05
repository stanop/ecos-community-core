/*
 * Copyright (C) 2008-2019 Citeck LLC.
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

public interface WorkingCalendarModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/wcalendar/1.0";

    public static final QName TYPE_CALENDAR = QName.createQName(NAMESPACE, "calendar");
    public static final QName PROP_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName PROP_YEAR = QName.createQName(NAMESPACE, "year");
    public static final QName PROP_WORKING_DAY_BEGIN = QName.createQName(NAMESPACE, "workingDayBegin");
    public static final QName PROP_WORKING_DAY_END = QName.createQName(NAMESPACE, "workingDayEnd");
    public static final QName PROP_COUNTRY = QName.createQName(NAMESPACE, "country");
    public static final QName PROP_SCHEDULE_NAME = QName.createQName(NAMESPACE, "scheduleName");
    public static final QName PROP_REGISTERED = QName.createQName(NAMESPACE, "registered");

}
