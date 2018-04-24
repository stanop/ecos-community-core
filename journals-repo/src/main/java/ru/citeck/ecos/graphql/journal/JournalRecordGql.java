package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;

import java.util.List;

public interface JournalRecordGql {

    @GraphQLField
    String id();

    @GraphQLField
    List<JournalAttributeGql> attributes(List<String> names);
}
