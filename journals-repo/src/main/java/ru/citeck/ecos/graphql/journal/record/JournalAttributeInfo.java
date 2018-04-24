package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;

public interface JournalAttributeInfo {

    @GraphQLField
    boolean hasRecords();
}
