package ru.citeck.ecos.dto;

/**
 * User has permission condition data transfer object
 */
public class UserHasPermissionConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "userHasPermissionCondition";

    /**
     * Permission
     */
    private String permission;

    /**
     * Permission username
     */
    private String permissionUsername;

    /** Getters and setters */

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermissionUsername() {
        return permissionUsername;
    }

    public void setPermissionUsername(String permissionUsername) {
        this.permissionUsername = permissionUsername;
    }
}
