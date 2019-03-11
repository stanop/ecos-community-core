package ru.citeck.ecos.history.records.model;


import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;

public class EventUserModel {

    private String id;

    private String displayName;

    @MetaAtt("cm:userName")
    private String userName;
    @MetaAtt("cm:firstName")
    private String firstName;
    @MetaAtt("cm:lastName")
    private String lastName;
    @MetaAtt("cm:middleName")
    private String middleName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
}
