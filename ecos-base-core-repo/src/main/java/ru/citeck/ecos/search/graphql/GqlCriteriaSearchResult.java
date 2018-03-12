package ru.citeck.ecos.search.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.DataFetchingEnvironment;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.SearchCriteria;

import java.util.List;

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
        return context.getNodes(results.getResults());
    }

    @GraphQLField
    public GqlCriteriaSearchResultPaging paging() {
        GqlCriteriaSearchResultPaging result = new GqlCriteriaSearchResultPaging();
        SearchCriteria criteria = results.getCriteria();
        result.hasMore = results.hasMore();
        result.totalCount = results.getResults().size();
        result.totalItems = results.getTotalCount();
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
        public Long totalItems = 0L;
        @GraphQLField
        public Integer totalCount = 0;
    }
}
