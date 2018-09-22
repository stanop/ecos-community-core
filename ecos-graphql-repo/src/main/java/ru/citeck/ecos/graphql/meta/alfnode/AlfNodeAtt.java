package ru.citeck.ecos.graphql.meta.alfnode;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AlfNodeAtt implements MetaAttribute {

    private String name;
    private Attribute attribute;
    private GqlContext context;
    private List<?> values;

    public AlfNodeAtt(Attribute attribute, GqlContext context) {
        this.attribute = attribute;
        this.context = context;
        this.name = attribute.name();
    }

    public AlfNodeAtt(String name, List<?> values, GqlContext context) {
        this.name = name;
        this.values = values;
        this.context = context;
    }

    public AlfNodeAtt(String name, Object value, GqlContext context) {
        this.name = name;
        this.context = context;
        if (value != null) {
            this.values = Collections.singletonList(value);
        } else {
            this.values = Collections.emptyList();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<MetaValue> val() {
        if (values == null) {
            values = attribute.getValues();
        }
        return values.stream()
                     .map(v -> new AlfNodeAttValue(v, context))
                     .collect(Collectors.toList());
    }
}
