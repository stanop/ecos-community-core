package ru.citeck.ecos.action;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;

import java.util.HashMap;
import java.util.Map;

public final class ActionConditionUtils {

    public static final String ACTION_CONDITION_VARIABLES = "action-condition-variables";
    public static final String PROCESS_VARIABLES = "process";

    public static Map<String,Object> getTransactionVariables() {
        Map<String, Object> variables = AlfrescoTransactionSupport.getResource(ActionConditionUtils.ACTION_CONDITION_VARIABLES);
        if (variables == null) {
            variables = new HashMap<>();
            variables.put(PROCESS_VARIABLES, new HashMap<>());
            AlfrescoTransactionSupport.bindResource(ACTION_CONDITION_VARIABLES, variables);
        }
        return variables;
    }

    public static Map<String,Object> getProcessVariables() {
        return (Map<String,Object>) getTransactionVariables().get(PROCESS_VARIABLES);
    }
}
