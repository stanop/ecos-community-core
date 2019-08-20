package ru.citeck.ecos.records.meta;

import ru.citeck.ecos.records2.QueryContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetaMapValue implements MetaValue {

    private String id;
    private Map<String, Object> attributes = Collections.emptyMap();
    private QueryContext context;

    public MetaMapValue(String id) {
        this.id = id;
    }

    @Override
    public <T extends QueryContext> void init(T context, MetaField field) {
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
    public List<MetaValue> getAttribute(String name, MetaField field) {
        Object value = attributes.get(name);
        return MetaUtils.toMetaValues(value, context, field);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}

