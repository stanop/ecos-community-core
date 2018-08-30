package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaReflectionAttribute;

import java.util.Optional;

public class MetaExplicitValue implements MetaValue {

    private Object val;

    public MetaExplicitValue(Object value) {
        if (value instanceof Optional) {
            val = ((Optional<?>) value).orElse(null);
        } else {
            val = value;
        }
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String str() {
        return val != null ? val.toString() : null;
    }

    @Override
    public Optional<MetaAttribute> att(String name) {
        return Optional.of(new MetaReflectionAttribute(val, name));
    }
}
