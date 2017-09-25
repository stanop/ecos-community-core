package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ActivityModel;

/**
 * @author Maxim Strizhov
 */
public class StagesModel {
    // model
    public static final String PREFIX = "stages";

    // namespace
    public static final String NAMESPACE = "http://www.citeck.ru/model/stages/1.0";

    // types
    public static final QName TYPE_STAGE = QName.createQName(NAMESPACE, "stage");

    // aspects
    public static final QName ASPECT_HAS_START_COMPLETENESS_LEVELS_RESTRICTION = QName.createQName(NAMESPACE, "hasStartCompletenessLevelsRestriction");
    public static final QName ASPECT_HAS_END_COMPLETENESS_LEVELS_RESTRICTION = QName.createQName(NAMESPACE, "hasStopCompletenessLevelsRestriction");

    // properties
    public static final QName PROP_STATE = QName.createQName(NAMESPACE, "state");
    public static final QName PROP_PLANNED_START_DATE = ActivityModel.PROP_PLANNED_START_DATE;
    public static final QName PROP_PLANNED_END_DATE = ActivityModel.PROP_PLANNED_END_DATE;
    public static final QName PROP_ACTUAL_START_DATE = ActivityModel.PROP_ACTUAL_START_DATE;
    public static final QName PROP_ACTUAL_END_DATE = ActivityModel.PROP_ACTUAL_END_DATE;
    public static final QName PROP_START_EVENT = QName.createQName(NAMESPACE, "startEvent");
    public static final QName PROP_STOP_EVENT = QName.createQName(NAMESPACE, "stopEvent");
    public static final QName PROP_START_EVENT_TIMER = QName.createQName(NAMESPACE, "startEventTimer");
    public static final QName PROP_STOP_EVENT_TIMER = QName.createQName(NAMESPACE, "stopEventTimer");
    public static final QName PROP_START_EVENT_LIFE_CYCLE_STAGE = QName.createQName(NAMESPACE, "startEventLifeCycleStage");
    public static final QName PROP_STOP_EVENT_LIFE_CYCLE_STAGE = QName.createQName(NAMESPACE, "stopEventLifeCycleStage");
    public static final QName PROP_DOCUMENT_STATUS = QName.createQName(NAMESPACE, "documentStatus");

    // association
    public static final QName ASSOC_CHILD_STAGES = QName.createQName(NAMESPACE, "childStages");
    public static final QName ASSOC_START_COMPLETENESS_LEVELS_RESTRICTION = QName.createQName(NAMESPACE, "startCompletenessLevelsRestriction");
    public static final QName ASSOC_STOP_COMPLETENESS_LEVELS_RESTRICTION = QName.createQName(NAMESPACE, "stopCompletenessLevelsRestriction");
    public static final QName ASSOC_START_EVENT_STAGE = QName.createQName(NAMESPACE, "startEventStage");
    public static final QName ASSOC_STOP_EVENT_STAGE = QName.createQName(NAMESPACE, "stopEventStage");
    public static final QName ASSOC_CASE_STATUS = QName.createQName(NAMESPACE, "caseStatusAssoc");

    // constraint
    public static final String CONSTR_USER_ACTION = "userAction";
    public static final String CONSTR_TIMER = "timer";
    public static final String CONSTR_LIFE_CYCLE_ENTER = "lifeCycleEnter";
    public static final String CONSTR_LIFE_CYCLE_EXIT = "lifeCycleExit";
    public static final String CONSTR_STAGE_START = "stageStart";
    public static final String CONSTR_STAGE_END = "stageEnd";

    public static final String CONSTR_STAGE_STATE_NOT_STARTED = "notStarted";
    public static final String CONSTR_STAGE_STATE_STARTED = "started";
    public static final String CONSTR_STAGE_STATE_STOPPED = "stopped";
}
