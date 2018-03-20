package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Case task data transfer object
 */
public class CaseTaskDto extends CaseModelDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "caseTask";

    /**
     * Task type full name
     */
    private String taskTypeFullName;

    /**
     * Workflow definition name
     */
    private String workflowDefinitionName;

    /**
     * Workflow instance id
     */
    private String workflowInstanceId;

    /**
     * Due date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dueDate;

    /**
     * Index
     */
    private Integer priority;

    /**
     * BPM Package
     */
    private BpmPackageDto bpmPackage;

    /**
     * Task properties
     */
    private String taskProperties;

    /** Getters and setters */

    public String getTaskTypeFullName() {
        return taskTypeFullName;
    }

    public void setTaskTypeFullName(String taskTypeFullName) {
        this.taskTypeFullName = taskTypeFullName;
    }

    public String getWorkflowDefinitionName() {
        return workflowDefinitionName;
    }

    public void setWorkflowDefinitionName(String workflowDefinitionName) {
        this.workflowDefinitionName = workflowDefinitionName;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public BpmPackageDto getBpmPackage() {
        return bpmPackage;
    }

    public void setBpmPackage(BpmPackageDto bpmPackage) {
        this.bpmPackage = bpmPackage;
    }

    public String getTaskProperties() {
        return taskProperties;
    }

    public void setTaskProperties(String taskProperties) {
        this.taskProperties = taskProperties;
    }
}
