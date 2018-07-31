package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Optional;

public interface JGqlAttributeValue {

    @GraphQLField
    String id();

    @GraphQLField
    String str();

    @GraphQLField
    Optional<JGqlAttribute> attr(@GraphQLName("name") String name);
}
