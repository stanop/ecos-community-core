package ru.citeck.ecos.dto;

/**
 * Fail case model data transfer object
 */
public class FailDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "fail";

    /**
     * Fail message
     */
    private String failMessage;

    /** Getters and setters */

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

}
