package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Collections;
import java.util.List;

public class JournalGqlPageInfoInput {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @GraphQLField
    private final int skipCount;
    @GraphQLField
    private final int maxItems;
    @GraphQLField
    private final List<JournalGqlSortBy> sortBy;

    public JournalGqlPageInfoInput(@GraphQLName("skipCount") Integer skipCount,
                                   @GraphQLName("maxItems") Integer maxItems,
                                   @GraphQLName("sortBy") List<JournalGqlSortBy> sortBy) {
        this.sortBy = sortBy != null ? sortBy : Collections.emptyList();
        this.maxItems = maxItems != null ? maxItems : DEFAULT_PAGE_SIZE;
        this.skipCount = skipCount != null ? skipCount : 0;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public List<JournalGqlSortBy> getSortBy() {
        return sortBy;
    }
}
