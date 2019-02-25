package ru.citeck.ecos.graphql.meta.value;

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

    public Object getValue() throws Exception {
        return scope.getAttribute(name);
    }
}
