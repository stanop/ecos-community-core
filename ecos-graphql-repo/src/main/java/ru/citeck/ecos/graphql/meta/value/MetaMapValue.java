package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.meta.attribute.MetaExplicitAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.Map;
import java.util.Optional;

public class MetaMapValue implements MetaValue {

    private String id;
    private Map<String, Object> attributes;

    public MetaMapValue(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String str() {
        return id;
    }

    @Override
    public Optional<MetaAttribute> att(String name) {
        Object value = attributes.get(name);
        MetaAttribute result;
        if (value instanceof MetaAttribute) {
            result = (MetaAttribute) value;
        } else {
            result = new MetaExplicitAttribute(name, value);
        }
        return Optional.of(result);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
