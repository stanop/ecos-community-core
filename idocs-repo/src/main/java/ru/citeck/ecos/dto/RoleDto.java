package ru.citeck.ecos.dto;

/**
 * Role data transfer object
 */
public class RoleDto extends AbstractEntityDto {

    /**
     * Var name
     */
    private String varName;

    /**
     * Is reference role
     */
    private Boolean isReferenceRole;

    /** Getters and setters */

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Boolean getIsReferenceRole() {
        return isReferenceRole;
    }

    public void setIsReferenceRole(Boolean referenceRole) {
        isReferenceRole = referenceRole;
    }
}
