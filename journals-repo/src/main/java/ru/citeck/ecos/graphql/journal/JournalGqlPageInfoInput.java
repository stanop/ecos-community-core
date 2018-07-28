package ru.citeck.ecos.graphql.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class JournalGqlPageInfoInput extends HashMap<String, Object> {

    private static final String PROP_SKIP_COUNT = "skipCount";
    private static final String PROP_MAX_ITEMS = "maxItems";
    private static final String PROP_SORT_BY = "sortBy";

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @GraphQLField
    private final int skipCount;
    @GraphQLField
    private final int maxItems;
    @GraphQLField
    private final List<JournalGqlSortBy> sortBy;

    @JsonCreator
    public JournalGqlPageInfoInput(
            @JsonProperty(PROP_SKIP_COUNT) @GraphQLName(PROP_SKIP_COUNT) Integer skipCount,
            @JsonProperty(PROP_MAX_ITEMS) @GraphQLName(PROP_MAX_ITEMS) Integer maxItems,
            @JsonProperty(PROP_SORT_BY) @GraphQLName(PROP_SORT_BY) List<JournalGqlSortBy> sortBy
    ) {
        super(3);

        this.sortBy = sortBy != null ? sortBy : Collections.emptyList();
        this.maxItems = maxItems != null ? maxItems : DEFAULT_PAGE_SIZE;
        this.skipCount = skipCount != null ? skipCount : 0;

        put(PROP_SKIP_COUNT, this.skipCount);
        put(PROP_MAX_ITEMS, this.maxItems);
        put(PROP_SORT_BY, this.sortBy);
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
