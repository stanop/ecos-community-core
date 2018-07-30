package ru.citeck.ecos.graphql.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.HashMap;

public class JournalGqlSortBy extends HashMap<String, Object> {

    static final String PROP_ATTRIBUTE = "attribute";
    static final String PROP_ORDER = "order";

    @GraphQLField
    private final String attribute;
    @GraphQLField
    private final String order;

    @JsonCreator
    public JournalGqlSortBy(
            @JsonProperty(PROP_ATTRIBUTE) @GraphQLName(PROP_ATTRIBUTE) String attribute,
            @JsonProperty(PROP_ORDER) @GraphQLName(PROP_ORDER) String order
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
}
