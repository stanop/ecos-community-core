package ru.citeck.ecos.dto;

/**
 * Set property value case model data transfer object
 */
public class SetPropertyValueDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "setPropertyValue";

    /**
     * Property full name
     */
    private String propertyFullName;

    /**
     * Property value
     */
    private String propertyValue;

    /** Getters and setters */

    public String getPropertyFullName() {
        return propertyFullName;
    }

    public void setPropertyFullName(String propertyFullName) {
        this.propertyFullName = propertyFullName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
