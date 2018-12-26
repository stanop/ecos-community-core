package ru.citeck.ecos.history.records.model;

import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

public class EventUserModel {

    public String id;

    public String displayName;

    @MetaAtt("cm:userName")
    public String userName;
    @MetaAtt("cm:firstName")
    public String firstName;
    @MetaAtt("cm:lastName")
    public String lastName;
    @MetaAtt("cm:middleName")
    public String middleName;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }
}
