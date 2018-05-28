package ru.citeck.ecos.dto;

/**
 * User in group condition data transfer object
 */
public class UserInGroupConditionDto extends ConditionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "userInGroupCondition";

    /**
     * Group name
     */
    private String groupName;

    /**
     * Group username
     */
    private String groupUsername;

    /** Getters and setters */

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupUsername() {
        return groupUsername;
    }

    public void setGroupUsername(String groupUsername) {
        this.groupUsername = groupUsername;
    }
}
