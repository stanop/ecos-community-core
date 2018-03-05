package ru.citeck.ecos.search.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.SearchCriteria;

import java.util.List;
import java.util.stream.Collectors;

public class GqlCriteriaSearchResult {

    private CriteriaSearchResults results;
    private String language;

    public GqlCriteriaSearchResult(CriteriaSearchResults results, String language) {
        this.results = results;
        this.language = language;
    }

    @GraphQLField
    public List<GqlAlfNode> results(DataFetchingEnvironment env) {
        GqlContext context = env.getContext();
        return results.getResults()
                      .stream()
                      .map(context::getNode)
                      .collect(Collectors.toList());
    }

    @GraphQLField
    public GqlCriteriaSearchResultPaging paging() {
        GqlCriteriaSearchResultPaging result = new GqlCriteriaSearchResultPaging();
        result.totalCount = results.getTotalCount();
        result.hasMore = results.hasMore();
        result.totalItems = results.getResults().size();
        SearchCriteria criteria = results.getCriteria();
        result.maxItems = criteria.getLimit();
        result.skipCount = criteria.getSkip();
        return result;
    }

    @GraphQLField
    public GqlCriteriaSearchResultQuery query() {
        GqlCriteriaSearchResultQuery result = new GqlCriteriaSearchResultQuery();
        result.value = results.getQuery();
        result.language = language;
        return result;
    }

    public static class GqlCriteriaSearchResultQuery {
        @GraphQLField
        public String language;
        @GraphQLField
        public String value;
    }

    public static class GqlCriteriaSearchResultPaging {
        @GraphQLField
        public Boolean hasMore = false;
        @GraphQLField
        public Integer maxItems = 0;
        @GraphQLField
        public Integer skipCount = 0;
        @GraphQLField
        public Integer totalItems = 0;
        @GraphQLField
        public Long totalCount = 0L;
    }
}
