package ru.citeck.ecos.dto;

/**
 * Compare process variable condition data transfer object
 */
public class CompareProcessVariableConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "compareProcessVariableCondition";

    /**
     * Process variable
     */
    private String processVariable;

    /**
     * Process variable value
     */
    private String processVariableValue;

    /** Getters and setters */

    public String getProcessVariable() {
        return processVariable;
    }

    public void setProcessVariable(String processVariable) {
        this.processVariable = processVariable;
    }

    public String getProcessVariableValue() {
        return processVariableValue;
    }

    public void setProcessVariableValue(String processVariableValue) {
        this.processVariableValue = processVariableValue;
    }
}
