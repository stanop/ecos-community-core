package ru.citeck.ecos.dto;

/**
 * Set process variable case model data transfer object
 */
public class SetProcessVariableDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "setProcessVariable";

    /**
     * Process variable name
     */
    private String processVariableName;

    /**
     * Process variable value
     */
    private String processVariableValue;

    /** Getters and setters */

    public String getProcessVariableName() {
        return processVariableName;
    }

    public void setProcessVariableName(String processVariableName) {
        this.processVariableName = processVariableName;
    }

    public String getProcessVariableValue() {
        return processVariableValue;
    }

    public void setProcessVariableValue(String processVariableValue) {
        this.processVariableValue = processVariableValue;
    }
}
