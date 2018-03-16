package ru.citeck.ecos.dto;

/**
 * Additional confirmer data item data transfer object
 */
public class AdditionalConfirmerDto extends AdditionalDataItemDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "additionalConfirmer";

    /**
     * Confirmer
     */
    private AuthorityDto confirmer;

    /** Getters and setters */

    public AuthorityDto getConfirmer() {
        return confirmer;
    }

    public void setConfirmer(AuthorityDto confirmer) {
        this.confirmer = confirmer;
    }
}
