package ru.citeck.ecos.graphql.meta.attribute;

import ru.citeck.ecos.graphql.meta.value.MetaExplicitValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetaExplicitAtt implements MetaAttribute {

    private String name;
    private List<MetaValue> values;

    public MetaExplicitAtt(String name, Object value) {
        this.name = name;
        if (value instanceof Collection) {
            values = ((Collection<?>) value).stream()
                    .map(MetaExplicitValue::new)
                    .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(new MetaExplicitValue(value));
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<MetaValue> val() {
        return values;
    }
}
