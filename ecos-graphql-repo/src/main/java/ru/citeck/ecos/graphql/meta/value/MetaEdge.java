package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;

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

    public List<MetaValue> getValue(GqlContext context) {
        return scope.getAttribute(name, context);
    }
}
