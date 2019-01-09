package ru.citeck.ecos.graphql.meta.value;

import java.util.List;

public class MetaEdge {

    private final String name;
    private final MetaValue scope;

    public MetaEdge(String name, MetaValue scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public List<MetaValue> getValue() {
        return scope.getAttribute(name);
    }
}
