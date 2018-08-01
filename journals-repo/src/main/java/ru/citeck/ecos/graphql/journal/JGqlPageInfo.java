package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;

import java.util.List;

public class JGqlPageInfo {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @GraphQLField
    private boolean hasNextPage;
    @GraphQLField
    private int skipCount;
    @GraphQLField
    private int maxItems = DEFAULT_PAGE_SIZE;
    @GraphQLField
    private List<JGqlSortBy> sortBy;
    @GraphQLField
    private String afterId;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public List<JGqlSortBy> getSortBy() {
        return sortBy;
    }

    public void setSortBy(List<JGqlSortBy> sortBy) {
        this.sortBy = sortBy;
    }

    public String getAfterId() {
        return afterId;
    }

    public void setAfterId(String afterId) {
        this.afterId = afterId;
    }

    public void set(JGqlPageInfoInput pageInfo) {
        skipCount = pageInfo.getSkipCount();
        maxItems = pageInfo.getMaxItems();
        sortBy = pageInfo.getSortBy();
        afterId = pageInfo.getAfterId();
    }
}
