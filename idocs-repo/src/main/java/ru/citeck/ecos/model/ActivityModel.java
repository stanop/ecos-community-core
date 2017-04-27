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

public class ActivityModel {
    public static final String PREFIX = "activ";
    public static final String NAMESPACE = "http://www.citeck.ru/model/activity/1.0";

    public static final QName TYPE_ACTIVITY = QName.createQName(NAMESPACE, "activity");
    public static final QName PROP_PLANNED_START_DATE = QName.createQName(NAMESPACE, "plannedStartDate");
    public static final QName PROP_PLANNED_END_DATE = QName.createQName(NAMESPACE, "plannedEndDate");
    public static final QName PROP_DAYS_NUMBER_TO_PLANNED_END_DATE = QName.createQName(NAMESPACE, "daysNumberToPlannedEndDate");
    public static final QName PROP_ACTUAL_START_DATE = QName.createQName(NAMESPACE, "actualStartDate");
    public static final QName PROP_ACTUAL_END_DATE = QName.createQName(NAMESPACE, "actualEndDate");
    public static final QName PROP_MANUAL_STARTED = QName.createQName(NAMESPACE, "manualStarted");
    public static final QName PROP_MANUAL_STOPPED = QName.createQName(NAMESPACE, "manualStopped");
    public static final QName PROP_INDEX = QName.createQName(NAMESPACE, "index");
    public static final QName PROP_REPEATABLE = QName.createQName(NAMESPACE, "repeatable");
    public static final QName PROP_EXPECTED_PERFORM_TIME = QName.createQName(NAMESPACE, "expectedPerformTime");

    public static final QName ASPECT_HAS_ACTIVITIES = QName.createQName(NAMESPACE, "hasActivities");
    public static final QName ASPECT_SET_PLANNED_END_DATE = QName.createQName(NAMESPACE, "setPlannedEndDate");
    public static final QName ASSOC_ACTIVITIES = QName.createQName(NAMESPACE, "activities");
}
