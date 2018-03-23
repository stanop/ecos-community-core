package ru.citeck.ecos.model;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.repo.workflow.StartWorkflowActionExecuter;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.action.FailActionExecuter;
import ru.citeck.ecos.action.ScriptParamActionExecuter;
import ru.citeck.ecos.action.SetCaseStatusActionExecuter;
import ru.citeck.ecos.action.SetLifecycleProcessVariableActionExecuter;

public final class ActionModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/action/1.0";
	public static final String PROP_DELIM = ":";

	public static final QName TYPE_ACTION = QName.createQName(NAMESPACE, "action");

    public static class SetPropertyValue {
        public static final QName TYPE = QName.createQName(NAMESPACE, SetPropertyValueActionExecuter.NAME);
        public static final QName PROP_PROPERTY = QName.createQName(TYPE + PROP_DELIM + SetPropertyValueActionExecuter.PARAM_PROPERTY);
        public static final QName PROP_VALUE = QName.createQName(TYPE + PROP_DELIM + SetPropertyValueActionExecuter.PARAM_VALUE);
    }
    
    public static class SetProcessVariable {
        public static final QName TYPE = QName.createQName(NAMESPACE, SetLifecycleProcessVariableActionExecuter.NAME);
        public static final QName PROP_VARIABLE = QName.createQName(TYPE + PROP_DELIM + SetLifecycleProcessVariableActionExecuter.PARAM_VARIABLE);
        public static final QName PROP_VALUE = QName.createQName(TYPE + PROP_DELIM + SetLifecycleProcessVariableActionExecuter.PARAM_VALUE);
    }
    
    public static class ExecuteScript {
        public static final QName TYPE = QName.createQName(NAMESPACE, ScriptParamActionExecuter.NAME);
        public static final QName PROP_SCRIPT = QName.createQName(TYPE + PROP_DELIM + ScriptParamActionExecuter.PARAM_SCRIPT);
    }

    public static class StartWorkflow {
        public static final QName TYPE = QName.createQName(NAMESPACE, StartWorkflowActionExecuter.NAME);
        public static final QName PROP_WORKFLOW_NAME = QName.createQName(TYPE + PROP_DELIM + StartWorkflowActionExecuter.PARAM_WORKFLOW_NAME);
    }
    
    public static class Mail {
        public static final QName TYPE = QName.createQName(NAMESPACE, MailActionExecuter.NAME);
        public static final QName PROP_TO = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_TO);
        public static final QName PROP_TO_MANY = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_TO_MANY);
        public static final QName PROP_SUBJECT = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_SUBJECT);
        public static final QName PROP_FROM = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_FROM);
        public static final QName PROP_TEXT = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_TEXT);
        public static final QName PROP_HTML = QName.createQName(TYPE + PROP_DELIM + MailActionExecuter.PARAM_HTML);
    }
    
    public static class Fail {
        public static final QName TYPE = QName.createQName(NAMESPACE, FailActionExecuter.NAME);
        public static final QName PROP_MESSAGE = QName.createQName(TYPE + PROP_DELIM + FailActionExecuter.PARAM_MESSAGE);
    }

    public static class SetCaseStatus {
        public static final QName TYPE = QName.createQName(NAMESPACE, SetCaseStatusActionExecuter.NAME);
        public static final QName PROP_STATUS = QName.createQName(TYPE + PROP_DELIM + SetCaseStatusActionExecuter.PARAM_STATUS);
    }
}
