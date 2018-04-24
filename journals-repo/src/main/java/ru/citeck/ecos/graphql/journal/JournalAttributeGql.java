package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;

import java.util.List;

public interface JournalAttributeGql {

    @GraphQLField
    String name();

    @GraphQLField
    List<String> values();

    @GraphQLField
    List<JournalRecordGql> records();
}
