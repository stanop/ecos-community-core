package ru.citeck.ecos.dto;

import java.util.List;

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
    /**
     * Success message
     */
    private String successMessage;
    /**
     * Success message css class
     */
    private String successMessageSpanClass;

    /**
     * Roles
     */
    private List<RoleDto> roles;

    /**
     * Additional data items
     */
    private List<AdditionalDataItemDto> additionalDataItems;

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

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    public List<AdditionalDataItemDto> getAdditionalDataItems() {
        return additionalDataItems;
    }

    public void setAdditionalDataItems(List<AdditionalDataItemDto> additionalDataItems) {
        this.additionalDataItems = additionalDataItems;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getSuccessMessageSpanClass() {
        return successMessageSpanClass;
    }

    public void setSuccessMessageSpanClass(String successMessageSpanClass) {
        this.successMessageSpanClass = successMessageSpanClass;
    }
}
