package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.List;

public interface JournalRecordGql {

    @GraphQLField
    String id();

    @GraphQLField
    List<JournalAttributeGql> attributes(@GraphQLName("names") List<String> names);

    JournalAttributeGql attribute(@GraphQLName("name") String name);
}
