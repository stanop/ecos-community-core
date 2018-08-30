package ru.citeck.ecos.graphql.meta.value;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.Optional;

public class MetaJsonObjectValue implements MetaValue {

    private String id;
    private ObjectNode data;

    public MetaJsonObjectValue(String id, ObjectNode data) {
        this.id = id;
        this.data = data;
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
        /*Object value = attributes.get(name);
        MetaAttribute result;
        if (value instanceof MetaAttribute) {
            result = (MetaAttribute) value;
        } else {
            result = new MetaExplicitAttribute(name, value);
        }
        return Optional.of(result);*/
        return null;
    }


}
