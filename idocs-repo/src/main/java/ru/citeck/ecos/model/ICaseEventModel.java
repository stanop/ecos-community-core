package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Pavel Simonov
 */
public class ICaseEventModel {
    public static final String PREFIX = "icaseEvent";

    public static final String NAMESPACE = "http://www.citeck.ru/model/icaseEvent/1.0";

    // properties
    public static final QName PROPERTY_TYPE = QName.createQName(NAMESPACE, "type");

    // types
    public static final QName TYPE_ACTIVITY_EVENT = QName.createQName(NAMESPACE, "activityEvent");
    public static final QName TYPE_ACTIVITY_STARTED_EVENT = QName.createQName(NAMESPACE, "activityStartedEvent");
    public static final QName TYPE_ACTIVITY_STOPPED_EVENT = QName.createQName(NAMESPACE, "activityStoppedEvent");
    public static final QName TYPE_STAGE_CHILDREN_STOPPED = QName.createQName(NAMESPACE, "stageChildrenStopped");
    public static final QName TYPE_CASE_CREATED = QName.createQName(NAMESPACE, "caseCreated");
    public static final QName TYPE_CASE_PROPERTIES_CHANGED = QName.createQName(NAMESPACE, "casePropertiesChanged");

    // associations
    public static final QName ASSOC_ACTIVITY_START_EVENTS = QName.createQName(NAMESPACE, "activityStartEvents");
    public static final QName ASSOC_ACTIVITY_END_EVENTS = QName.createQName(NAMESPACE, "activityEndEvents");
    public static final QName ASSOC_ACTIVITY_RESTART_EVENTS = QName.createQName(NAMESPACE, "activityRestartEvents");
    public static final QName ASSOC_ACTIVITY_RESET_EVENTS = QName.createQName(NAMESPACE, "activityResetEvents");

    // constraints
    public static final String CONSTR_ACTIVITY_STARTED = "activity-started";
    public static final String CONSTR_ACTIVITY_STOPPED = "activity-stopped";
    public static final String CONSTR_STAGE_CHILDREN_STOPPED = "stage-children-stopped";
    public static final String CONSTR_CASE_CREATED = "case-created";
    public static final String CONSTR_CASE_PROPERTIES_CHANGED = "case-properties-changed";
}
