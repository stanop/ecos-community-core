package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Additional data item data transfer object
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdditionalPerformersDto.class, name = AdditionalPerformersDto.DTO_TYPE),
        @JsonSubTypes.Type(value = AdditionalConfirmerDto.class, name = AdditionalConfirmerDto.DTO_TYPE),
        @JsonSubTypes.Type(value = AdditionalDataItemDto.class, name = AdditionalDataItemDto.DTO_TYPE)
})
public class AdditionalDataItemDto extends AbstractEntityDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "additionalDataItem";

    /**
     * Comment
     */
    private String comment;

    /** Getters and setters */

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
