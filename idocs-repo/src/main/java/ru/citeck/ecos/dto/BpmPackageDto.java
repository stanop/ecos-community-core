package ru.citeck.ecos.dto;

/**
 * BPM package data transfer
 */
public class BpmPackageDto extends AbstractEntityDto {

    /**
     * Workflow instance id
     */
    private String workflowInstanceId;

    /**
     * Workflow definition name
     */
    private String workflowDefinitionName;

    /**
     * Is system package
     */
    private Boolean isSystemPackage;

    /** Getters and setters */

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getWorkflowDefinitionName() {
        return workflowDefinitionName;
    }

    public void setWorkflowDefinitionName(String workflowDefinitionName) {
        this.workflowDefinitionName = workflowDefinitionName;
    }

    public Boolean getSystemPackage() {
        return isSystemPackage;
    }

    public void setSystemPackage(Boolean systemPackage) {
        isSystemPackage = systemPackage;
    }
}
