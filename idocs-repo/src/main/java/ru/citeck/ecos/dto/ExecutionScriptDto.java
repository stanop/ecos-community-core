package ru.citeck.ecos.dto;

/**
 * Execution script data transfer object
 */
public class ExecutionScriptDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "executionScript";

    /**
     * Execute script
     */
    private String executeScript;

    /** Getters and setters */

    public String getExecuteScript() {
        return executeScript;
    }

    public void setExecuteScript(String executeScript) {
        this.executeScript = executeScript;
    }

}
