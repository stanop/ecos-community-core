package ru.citeck.ecos.graphql.meta.attribute;

import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

public class MetaEmptyAtt implements MetaAttribute {

    private String name;

    public MetaEmptyAtt() {
    }

    public MetaEmptyAtt(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<MetaValue> val() {
        return Collections.emptyList();
    }
}
