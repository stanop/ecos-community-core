package ru.citeck.ecos.dto;

import java.util.List;

/**
 * Additional performers data item data transfer object
 */
public class AdditionalPerformersDto extends AdditionalDataItemDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "additionalPerformers";

    /**
     * Performers
     */
    private List<AuthorityDto> performers;

    /** Getters and setters */

    public List<AuthorityDto> getPerformers() {
        return performers;
    }

    public void setPerformers(List<AuthorityDto> performers) {
        this.performers = performers;
    }
}
