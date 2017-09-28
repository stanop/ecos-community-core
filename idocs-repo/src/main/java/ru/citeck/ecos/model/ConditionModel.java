package ru.citeck.ecos.model;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.action.evaluator.CompareLifecycleProcessVariableValueEvaluator;
import ru.citeck.ecos.action.evaluator.ScriptEvaluator;
import ru.citeck.ecos.action.evaluator.UserHasPermissionEvaluator;
import ru.citeck.ecos.action.evaluator.UserInDocumentEvaluator;
import ru.citeck.ecos.action.evaluator.UserInGroupEvaluator;

public final class ConditionModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/condition/1.0";
	public static final String PROP_DELIM = ":";

	public static final QName TYPE_CONDITION = QName.createQName(NAMESPACE, "condition");

    public static class ComparePropertyValue {
        public static final QName TYPE = QName.createQName(NAMESPACE, ComparePropertyValueEvaluator.NAME);
        public static final QName PROP_PROPERTY = QName.createQName(TYPE + PROP_DELIM + ComparePropertyValueEvaluator.PARAM_PROPERTY);
        public static final QName PROP_OPERATION = QName.createQName(TYPE + PROP_DELIM + ComparePropertyValueEvaluator.PARAM_OPERATION);
        public static final QName PROP_VALUE = QName.createQName(TYPE + PROP_DELIM + ComparePropertyValueEvaluator.PARAM_VALUE);
    }
    
    public static class CompareProcessVariable {
        public static final QName TYPE = QName.createQName(NAMESPACE, CompareLifecycleProcessVariableValueEvaluator.NAME);
        public static final QName PROP_VARIABLE = QName.createQName(TYPE + PROP_DELIM + CompareLifecycleProcessVariableValueEvaluator.PARAM_VARIABLE);
        public static final QName PROP_VALUE = QName.createQName(TYPE + PROP_DELIM + CompareLifecycleProcessVariableValueEvaluator.PARAM_VALUE);
    }
    
    public static class EvaluateScript {
        public static final QName TYPE = QName.createQName(NAMESPACE, ScriptEvaluator.NAME);
        public static final QName PROP_SCRIPT = QName.createQName(TYPE + PROP_DELIM + ScriptEvaluator.PARAM_SCRIPT);
    }

    public static class UserInDocument {
        public static final QName TYPE = QName.createQName(NAMESPACE, UserInDocumentEvaluator.NAME);
        public static final QName PROP_PROPERTY = QName.createQName(TYPE + PROP_DELIM + UserInDocumentEvaluator.PARAM_PROPERTY);
        public static final QName PROP_USERNAME = QName.createQName(TYPE + PROP_DELIM + UserInDocumentEvaluator.PARAM_USER);
    }
    
    public static class UserInGroup {
        public static final QName TYPE = QName.createQName(NAMESPACE, UserInGroupEvaluator.NAME);
        public static final QName PROP_GROUPNAME = QName.createQName(TYPE + PROP_DELIM + UserInGroupEvaluator.PARAM_GROUP);
        public static final QName PROP_USERNAME = QName.createQName(TYPE + PROP_DELIM + UserInGroupEvaluator.PARAM_USER);
    }
    
    public static class UserHasPermission {
        public static final QName TYPE = QName.createQName(NAMESPACE, UserHasPermissionEvaluator.NAME);
        public static final QName PROP_PERMISSION = QName.createQName(TYPE + PROP_DELIM + UserHasPermissionEvaluator.PARAM_PERMISSION);
        public static final QName PROP_USERNAME = QName.createQName(TYPE + PROP_DELIM + UserHasPermissionEvaluator.PARAM_USER);
    }
    
}
