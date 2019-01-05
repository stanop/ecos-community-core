package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;

import java.util.*;

public class MetaMapValue implements MetaValue {

    private String id;
    private Map<String, Object> attributes = Collections.emptyMap();

    public MetaMapValue(String id) {
        this.id = id;
    }

    @Override
    public String getId(GqlContext context) {
        return id;
    }

    @Override
    public String getString(GqlContext context) {
        return id;
    }

    @Override
    public List<MetaValue> getAttribute(String name, GqlContext context) {
        Object value = attributes.get(name);
        return MetaUtils.toMetaValues(value);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
