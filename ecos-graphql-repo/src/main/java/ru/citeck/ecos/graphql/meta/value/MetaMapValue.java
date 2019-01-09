package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;

import java.util.*;

public class MetaMapValue implements MetaValue {

    private String id;
    private Map<String, Object> attributes = Collections.emptyMap();
    private GqlContext context;

    public MetaMapValue(String id) {
        this.id = id;
    }

    @Override
    public MetaValue init(GqlContext context) {
        this.context = context;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getString() {
        return id;
    }

    @Override
    public List<MetaValue> getAttribute(String name) {
        Object value = attributes.get(name);
        return MetaUtils.toMetaValues(value, context);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
