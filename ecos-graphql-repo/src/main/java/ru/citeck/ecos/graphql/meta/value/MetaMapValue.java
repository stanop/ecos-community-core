package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.meta.attribute.MetaExplicitAtt;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.*;

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
        return Optional.of(toMetaAtt(name, value));
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        List<MetaAttribute> result = new ArrayList<>();
        attributes.forEach((name, value) -> result.add(toMetaAtt(name, value)));
        return result;
    }

    private MetaAttribute toMetaAtt(String name, Object value) {
        MetaAttribute result;
        if (value instanceof MetaAttribute) {
            result = (MetaAttribute) value;
        } else {
            result = new MetaExplicitAtt(name, value);
        }
        return result;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
