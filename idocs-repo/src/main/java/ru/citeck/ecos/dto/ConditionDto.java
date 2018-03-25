package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Condition data transfer object
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompareProcessVariableConditionDto.class, name = CompareProcessVariableConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ComparePropertyValueConditionDto.class, name = ComparePropertyValueConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = EvaluateScriptConditionDto.class, name = EvaluateScriptConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = UserHasPermissionConditionDto.class, name = UserHasPermissionConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = UserInDocumentConditionDto.class, name = UserInDocumentConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = UserInGroupConditionDto.class, name = UserInGroupConditionDto.DTO_TYPE),
        @JsonSubTypes.Type(value = ConditionDto.class, name = ConditionDto.DTO_TYPE)
})
public class ConditionDto extends AbstractEntityDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "condition";
}
