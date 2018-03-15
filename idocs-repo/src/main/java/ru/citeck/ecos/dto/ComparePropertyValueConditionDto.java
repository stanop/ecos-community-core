package ru.citeck.ecos.dto;

/**
 * Compare property value condition data transfer object
 */
public class ComparePropertyValueConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "comparePropertyValueCondition";

    /**
     * Property name
     */
    private String propertyName;

    /**
     * Property value
     */
    private String propertyValue;

    /**
     * Property operation
     */
    private String propertyOperation;

    /** Getters and setters */

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyOperation() {
        return propertyOperation;
    }

    public void setPropertyOperation(String propertyOperation) {
        this.propertyOperation = propertyOperation;
    }
}
