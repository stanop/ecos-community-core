package ru.citeck.ecos.dto;

/**
 * Task property data transfer object
 */
public class TaskPropertyDto {

    /**
     * Type name
     */
    private String typeName;

    /**
     * Value class
     */
    private String valueClass;

    /**
     * Raw value
     */
    private String value;

    /** Getters and setters */

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getValueClass() {
        return valueClass;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
