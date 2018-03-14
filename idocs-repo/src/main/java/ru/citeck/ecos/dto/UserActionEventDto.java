package ru.citeck.ecos.dto;

/**
 * User action event data transfer object
 */
public class UserActionEventDto extends EventDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "userActionEvent";

    /**
     * Additional data type
     */
    private String additionalDataType;

    /**
     * Confirmation message
     */
    private String confirmationMessage;

    /** Getters and setters */

    public String getAdditionalDataType() {
        return additionalDataType;
    }

    public void setAdditionalDataType(String additionalDataType) {
        this.additionalDataType = additionalDataType;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

}
