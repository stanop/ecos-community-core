package ru.citeck.ecos.history.records.model;

import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

public class EventUserModel {

    public String id;

    public String displayName;

    @MetaAtt(name = "cm:userName")
    public String userName;
    @MetaAtt(name = "cm:firstName")
    public String firstName;
    @MetaAtt(name = "cm:lastName")
    public String lastName;
    @MetaAtt(name = "cm:middleName")
    public String middleName;
}
