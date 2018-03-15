package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Event data transfer object
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActivityEventDto.class, name = ActivityEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ActivityStartedEventDto.class, name = ActivityStartedEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ActivityStoppedEventDto.class, name = ActivityStoppedEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = CaseCreatedEventDto.class, name = CaseCreatedEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = CasePropertiesChangedEventDto.class, name = CasePropertiesChangedEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = StageChildrenStoppedEventDto.class, name = StageChildrenStoppedEventDto.DTO_TYPE),
        @JsonSubTypes.Type(value = UserActionEventDto.class, name = UserActionEventDto.DTO_TYPE)
})
public class EventDto extends AbstractEntityDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "event";

    /**
     * Event type
     */
    private String type;

    /**
     * Source case
     */
    private String sourceCaseId;

    /**
     * Is source case
     */
    private Boolean isSourceCase;

    /**
     * Conditions
     */
    private List<ConditionDto> conditions;

    /** Getters and setters */

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceCaseId() {
        return sourceCaseId;
    }

    public void setSourceCaseId(String sourceCaseId) {
        this.sourceCaseId = sourceCaseId;
    }

    public Boolean getIsSourceCase() {
        return isSourceCase;
    }

    public void setIsSourceCase(Boolean sourceCase) {
        isSourceCase = sourceCase;
    }

    public List<ConditionDto> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionDto> conditions) {
        this.conditions = conditions;
    }
}
