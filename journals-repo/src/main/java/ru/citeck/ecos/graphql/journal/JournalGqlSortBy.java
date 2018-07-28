package ru.citeck.ecos.graphql.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.HashMap;

public class JournalGqlSortBy extends HashMap<String, Object> {

    private static final String PARAM_ATTRIBUTE = "attribute";
    private static final String PARAM_ORDER = "order";

    @GraphQLField
    private final String attribute;
    @GraphQLField
    private final String order;

    @JsonCreator
    public JournalGqlSortBy(
            @JsonProperty(PARAM_ATTRIBUTE) @GraphQLName(PARAM_ATTRIBUTE) String attribute,
            @JsonProperty(PARAM_ORDER) @GraphQLName(PARAM_ORDER) String order
    ) {
        super(2);

        this.attribute = attribute;
        this.order = order;

        put(PARAM_ATTRIBUTE, this.attribute);
        put(PARAM_ORDER, this.order);
    }

    public String getAttribute() {
        return attribute;
    }

    public String getOrder() {
        return order;
    }
}
