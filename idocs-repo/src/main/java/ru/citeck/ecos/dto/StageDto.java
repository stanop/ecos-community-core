package ru.citeck.ecos.dto;

/**
 * Stage data transfer object
 */
public class StageDto extends CaseModelDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "stage";

    /**
     * Document status
     */
    private String documentStatus;

    /** Getters and setters */

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

}
