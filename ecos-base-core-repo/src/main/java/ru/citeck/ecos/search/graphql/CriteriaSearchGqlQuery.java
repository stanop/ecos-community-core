package ru.citeck.ecos.search.graphql;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.service.CiteckServices;

import java.util.function.Supplier;

@Component
@GraphQLQueryDefinition
public class CriteriaSearchGqlQuery {

    private static SearchCriteriaParser parser;

    @GraphQLField
    public static GqlCriteriaSearchResult criteriaSearch(DataFetchingEnvironment env,
                                                         @GraphQLName("q") String query,
                                                         @GraphQLName("lang")
                                                         @GraphQLDefaultValue(DefaultSearchLanguage.class)
                                                         String language) {

        GqlContext context = env.getContext();
        CriteriaSearchService searchService = context.getService(CiteckServices.CRITERIA_SEARCH_SERVICE);
        SearchCriteria criteria = parser.parse(query);

        CriteriaSearchResults results = searchService.query(criteria, language);

        return new GqlCriteriaSearchResult(results, language);
    }

    @Autowired
    public void setParser(SearchCriteriaParser parser) {
        CriteriaSearchGqlQuery.parser = parser;
    }

    public static class DefaultSearchLanguage implements Supplier<Object> {
        @Override
        public Object get() {
            return SearchService.LANGUAGE_FTS_ALFRESCO;
        }
    }
}
