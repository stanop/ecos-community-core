package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Case model data transfer object
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StageDto.class, name = StageDto.DTO_TYPE),
        @JsonSubTypes.Type(value = CaseModelDto.class, name = CaseModelDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ActionDto.class, name = ActionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ExecutionScriptDto.class, name = ExecutionScriptDto.DTO_TYPE),
        @JsonSubTypes.Type(value = FailDto.class, name = FailDto.DTO_TYPE),
        @JsonSubTypes.Type(value = MailDto.class, name = MailDto.DTO_TYPE),
        @JsonSubTypes.Type(value = SetProcessVariableDto.class, name = SetProcessVariableDto.DTO_TYPE),
        @JsonSubTypes.Type(value = SetPropertyValueDto.class, name = SetPropertyValueDto.DTO_TYPE),
        @JsonSubTypes.Type(value = StartWorkflowDto.class, name = StartWorkflowDto.DTO_TYPE)
})
public class CaseModelDto extends AbstractEntityDto implements Serializable {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "caseModel";

    /**
     * Node uuid
     */
    private String nodeUUID;

    /**
     * Document id
     */
    private String documentId;

    /**
     * Planned start date
     */
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date plannedStartDate;

    /**
     * Planned end date
     */
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date plannedEndDate;

    /**
     * Actual start date
     */
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date actualStartDate;

    /**
     * Actual end date
     */
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date actualEndDate;

    /**
     * Expected perform time
     */
    private Integer expectedPerformTime;

    /**
     * Manual started
     */
    private Boolean manualStarted;

    /**
     * Manual stopped
     */
    private Boolean manualStopped;

    /**
     * Index
     */
    private Integer index;

    /**
     * Auto events
     */
    private Boolean autoEvents;

    /**
     * Repeatable
     */
    private Boolean repeatable;

    /**
     * Type version
     */
    private Integer typeVersion;

    /**
     * Child cases
     */
    private List<CaseModelDto> childCases;

    /**
     * Has child cases
     */
    private Boolean hasChildCases;

    /** Getters and setters */

    public String getNodeUUID() {
        return nodeUUID;
    }

    public void setNodeUUID(String nodeUUID) {
        this.nodeUUID = nodeUUID;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Date getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(Date plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public Date getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(Date plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Integer getExpectedPerformTime() {
        return expectedPerformTime;
    }

    public void setExpectedPerformTime(Integer expectedPerformTime) {
        this.expectedPerformTime = expectedPerformTime;
    }

    public Boolean getManualStarted() {
        return manualStarted;
    }

    public void setManualStarted(Boolean manualStarted) {
        this.manualStarted = manualStarted;
    }

    public Boolean getManualStopped() {
        return manualStopped;
    }

    public void setManualStopped(Boolean manualStopped) {
        this.manualStopped = manualStopped;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Boolean getAutoEvents() {
        return autoEvents;
    }

    public void setAutoEvents(Boolean autoEvents) {
        this.autoEvents = autoEvents;
    }

    public Boolean getRepeatable() {
        return repeatable;
    }

    public void setRepeatable(Boolean repeatable) {
        this.repeatable = repeatable;
    }

    public Integer getTypeVersion() {
        return typeVersion;
    }

    public void setTypeVersion(Integer typeVersion) {
        this.typeVersion = typeVersion;
    }

    public List<CaseModelDto> getChildCases() {
        return childCases;
    }

    public void setChildCases(List<CaseModelDto> childCases) {
        this.childCases = childCases;
    }

    public Boolean getHasChildCases() {
        return hasChildCases;
    }

    public void setHasChildCases(Boolean hasChildCases) {
        this.hasChildCases = hasChildCases;
    }
}
