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

    /**
     * Case status
     */
    private CaseStatusDto caseStatus;

    /** Getters and setters */

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    /** Getters and setters */

    public CaseStatusDto getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(CaseStatusDto caseStatus) {
        this.caseStatus = caseStatus;
    }

}
