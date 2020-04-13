package ru.citeck.ecos.icase.activity.service.eproc.parser;

public interface CmmnDefinitionConstants {

    String DOCUMENT_STATUS = "documentStatus";
    String CASE_STATUS = "caseStatus";

    String TASK_ROLE_VAR_NAMES_SET_KEY = "taskRoleVarNames";

    String WORKFLOW_DEFINITION_NAME = "workflowDefinitionName";
    String USE_ACTIVITY_TITLE = "useActivityTitle";
    String TITLE = "title";
    String EXPECTED_PERFORM_TIME = "expectedPerformTime";
    String PRIORITY = "priority";

    //action props
    String ACTION_TYPE = "actionType";
    String ACTION_SET_PROPERTY_PROP_NAME = "set-property-value:property";
    String ACTION_SET_PROPERTY_PROP_VALUE = "set-property-value:value";
    String ACTION_SET_PROCESS_VAR_NAME = "set-process-variable:variable";
    String ACTION_SET_PROCESS_VAR_VALUE = "set-process-variable:value";
    String ACTION_SET_STATUS_ACTION_STATUS_NAME = "actionCaseStatus";
    String ACTION_SEND_WORKFLOW_SIGNAL_NAME = "send-workflow-signal:signalName";
    String ACTION_SCRIPT = "execute-script:script";
    String ACTION_FAIL_MESSAGE = "fail:message";

    //conditions
    String CONDITION_TYPE = "conditionType";

}
