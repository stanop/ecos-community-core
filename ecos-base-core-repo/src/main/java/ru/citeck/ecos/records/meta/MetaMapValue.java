package ru.citeck.ecos.records.meta;

import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetaMapValue implements MetaValue {

    private String id;
    private Map<String, Object> attributes = Collections.emptyMap();
    private GqlContext context;

    public MetaMapValue(String id) {
        this.id = id;
    }

    @Override
    public <T extends GqlContext> void init(T context) {
        this.context = context;
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

