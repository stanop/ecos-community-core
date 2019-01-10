package ru.citeck.ecos.graphql.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class JGqlSortBy extends HashMap<String, Object> {

    static final String PROP_ATTRIBUTE = "attribute";
    static final String PROP_ORDER = "order";

    private final String attribute;
    private final String order;

    @JsonCreator
    public JGqlSortBy(
            @JsonProperty(PROP_ATTRIBUTE) String attribute,
            @JsonProperty(PROP_ORDER) String order
    ) {
        super(2);

        this.attribute = attribute;
        this.order = order;

        put(PROP_ATTRIBUTE, this.attribute);
        put(PROP_ORDER, this.order);
    }

    public String getAttribute() {
        return attribute;
    }

    public String getOrder() {
        return order;
    }

    public boolean isAscending() {
        return "asc".equalsIgnoreCase(order);
    }
}
