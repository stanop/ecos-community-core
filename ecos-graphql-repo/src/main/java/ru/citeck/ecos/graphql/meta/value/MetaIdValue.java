package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ParameterCheck;

public class MetaIdValue implements MetaValue {

    private String id;

    public MetaIdValue(Object id) {
        ParameterCheck.mandatory("id", id);
        this.id = id.toString();
    }

    @Override
    public String getString() {
        return id;
    }

    @Override
    public String getId() {
        return id;
    }
}
