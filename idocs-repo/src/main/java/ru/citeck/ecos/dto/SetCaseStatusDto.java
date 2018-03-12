package ru.citeck.ecos.dto;

/**
 * Set case status data transfer object
 */
public class SetCaseStatusDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "setCaseStatus";

    /**
     * Case status
     */
    private CaseStatusDto caseStatus;

    /** Getters and setters */

    public CaseStatusDto getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(CaseStatusDto caseStatus) {
        this.caseStatus = caseStatus;
    }
}
