package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;

import java.util.List;

public interface JGqlAttribute {

    @GraphQLField
    String name();

    @GraphQLField
    List<JGqlAttributeValue> val();
}
