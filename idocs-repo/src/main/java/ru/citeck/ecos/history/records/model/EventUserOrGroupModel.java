package ru.citeck.ecos.history.records.model;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

public class EventUserOrGroupModel {

    public NodeRef id;

    @MetaAtt("cm:authorityName")
    private String authorityName;

    @MetaAtt("cm:userName")
    private String userName;

    public NodeRef getId() {
        return id;
    }

    public void setId(NodeRef id) {
        this.id = id;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
