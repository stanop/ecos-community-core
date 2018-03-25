package ru.citeck.ecos.dto;

/**
 * case model data transfer object
 */
public class StartWorkflowDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "startWorkflow";

    /**
     * Workflow name
     */
    private String workflowName;

    /** Getters and setters */

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }
}
