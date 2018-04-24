package ru.citeck.ecos.graphql.journal.datasource;

import graphql.annotations.annotationTypes.GraphQLField;

public class JournalDataSourcePaging {
    @GraphQLField
    public Boolean hasMore = false;
    @GraphQLField
    public Integer maxItems = 0;
    @GraphQLField
    public Integer skipCount = 0;
    @GraphQLField
    public Long totalItems = 0L;
    @GraphQLField
    public Integer totalCount = 0;
}
