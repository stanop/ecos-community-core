package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.graphql.GqlContext;

public class MetaIdValue implements MetaValue {

    private String id;

    public MetaIdValue(Object id) {
        ParameterCheck.mandatory("id", id);
        this.id = id.toString();
    }

    @Override
    public String getString(GqlContext context) {
        return id;
    }

    @Override
    public String getId(GqlContext context) {
        return id;
    }
}
