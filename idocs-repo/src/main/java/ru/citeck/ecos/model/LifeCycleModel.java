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

/**
 * @author: Alexander Nemerov
 * @author: Alexey Moiseev
 * @date: 19.02.14
 */
public class LifeCycleModel {

    // model
    public static final String PREFIX = "lc";

    // namespace
    public static final String NAMESPACE = "http://www.citeck.ru/model/lifecycle/1.0";

    // types
    public static final QName TYPE_LIFECYCLE_DEFINITION = QName.createQName(NAMESPACE, "lifecycleDefinition");
    public static final QName PROP_LIFECYCLE_ENABLED = QName.createQName(NAMESPACE, "lifecycleEnabled");
    public static final QName PROP_LIFECYCLE_FORMAT = QName.createQName(NAMESPACE, "lifecycleFormat");
    public static final QName PROP_LIFECYCLE_TITLE = QName.createQName(NAMESPACE, "lifecycleTitle");
    //public static final QName TYPE_STATUS = QName.createQName(NAMESPACE, "status");
    //public static final QName TYPE_TRANSITION_RULE = QName.createQName(NAMESPACE, "transitionRule");
    //public static final QName TYPE_TRANSITION_TABLE = QName.createQName(NAMESPACE, "transitionTable");
    //public static final QName TYPE_ACTION = QName.createQName(NAMESPACE, "action");

    // aspects
    public static final QName ASPECT_HAS_STATE = QName.createQName(NAMESPACE, "hasState");
    public static final QName ASPECT_HAS_TIMER = QName.createQName(NAMESPACE, "hasTimer");
    public static final QName ASPECT_HAS_DOC_TYPE = QName.createQName(NAMESPACE, "hasDocType");

    // properties
    //public static final QName PROP_STATUS_ID = QName.createQName(NAMESPACE, "statusID");
    //public static final QName PROP_STATUS_NAME = QName.createQName(NAMESPACE, "statusName");
    //public static final QName PROP_ACTION_CAPTION = QName.createQName(NAMESPACE, "actionCaption");
    //public static final QName PROP_TRANSITION_CONDITION = QName.createQName(NAMESPACE, "transitionCondition");
    //public static final QName PROP_FROM_STATE = QName.createQName(NAMESPACE, "fromState");
    //public static final QName PROP_TO_STATE = QName.createQName(NAMESPACE, "toState");
    public static final QName PROP_DOC_TYPE = QName.createQName(NAMESPACE, "docType");
    //public static final QName PROP_ACTION = QName.createQName(NAMESPACE, "action");
    public static final QName PROP_STATE = QName.createQName(NAMESPACE, "state");
    //public static final QName PROP_EVENT = QName.createQName(NAMESPACE, "event");
    public static final QName PROP_EVENT_TIME = QName.createQName(NAMESPACE, "eventTime");

    // association
    //public static final QName ASSOC_TO_STATUS = QName.createQName(NAMESPACE, "toStatus");
    //public static final QName ASSOC_ACTION = QName.createQName(NAMESPACE, "action");
    //public static final QName ASSOC_TRANSITION_RULES = QName.createQName(NAMESPACE, "transitionRules");
    //public static final QName ASSOC_TRANSITION_TABLE = QName.createQName(NAMESPACE, "transitionTable");
    //public static final QName ASSOC_STATUS = QName.createQName(NAMESPACE, "status");

    // constraint
    public static final String CONSTR_AUTOMATIC_TRANSITION = "automaticTransition";
    public static final String CONSTR_USER_TRANSITION = "userTransition";
    public static final String CONSTR_TRANSITION_ON_START_PROCESS = "onStartProcess";
    public static final String CONSTR_TRANSITION_ON_END_PROCESS = "onEndProcess";
    public static final String CONSTR_TRANSITION_ON_SIGNAL = "onSignal";
    public static final String CONSTR_TIMER_TRANSITION = "timerTransition";
    public static final String CONSTR_XML = "xml";
    public static final String CONSTR_CSV = "csv";

}
