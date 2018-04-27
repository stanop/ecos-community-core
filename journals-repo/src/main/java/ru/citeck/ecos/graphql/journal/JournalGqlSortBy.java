package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

public class JournalGqlSortBy {

    @GraphQLField
    private final String attribute;
    @GraphQLField
    private final String order;

    public JournalGqlSortBy(@GraphQLName("attribute") String attribute,
                            @GraphQLName("order") String order) {
        this.attribute = attribute;
        this.order = order;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getOrder() {
        return order;
    }
}
