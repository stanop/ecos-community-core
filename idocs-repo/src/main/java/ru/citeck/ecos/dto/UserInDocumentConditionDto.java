package ru.citeck.ecos.dto;

/**
 * User in document condition data transfer object
 */
public class UserInDocumentConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "userInDocumentCondition";

    /**
     * Document property
     */
    private String documentProperty;

    /**
     * Document username
     */
    private String documentUsername;

    /** Getters and setters */

    public String getDocumentProperty() {
        return documentProperty;
    }

    public void setDocumentProperty(String documentProperty) {
        this.documentProperty = documentProperty;
    }

    public String getDocumentUsername() {
        return documentUsername;
    }

    public void setDocumentUsername(String documentUsername) {
        this.documentUsername = documentUsername;
    }
}
