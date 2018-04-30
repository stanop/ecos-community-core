package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Optional;

public interface JournalAttributeValueGql {

    @GraphQLField
    String id();

    @GraphQLField
    String data();

    @GraphQLField
    Optional<JournalAttributeGql> attr(@GraphQLName("name") String name);
}
