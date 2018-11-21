package ru.citeck.ecos.history.records.model;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

public class EventUserOrGroupModel {

    public NodeRef id;

    @MetaAtt(name = "cm:authorityName")
    public String authorityName;

    @MetaAtt(name = "cm:userName")
    public String userName;

    public NodeRef getId() {
        return id;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public String getUserName() {
        return userName;
    }
}
