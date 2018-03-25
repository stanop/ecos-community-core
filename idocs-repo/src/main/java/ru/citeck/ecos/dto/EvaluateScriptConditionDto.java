package ru.citeck.ecos.dto;

/**
 * Evaluate script condition data transfer object
 */
public class EvaluateScriptConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "evaluateScriptCondition";

    /**
     * Evaluate script
     */
    private String evaluateScript;

    /** Getters and setters */

    public String getEvaluateScript() {
        return evaluateScript;
    }

    public void setEvaluateScript(String evaluateScript) {
        this.evaluateScript = evaluateScript;
    }
}
