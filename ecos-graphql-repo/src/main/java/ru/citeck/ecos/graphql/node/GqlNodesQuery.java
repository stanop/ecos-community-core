package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.*;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import ru.citeck.ecos.graphql.Defaults;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@GraphQLQueryDefinition
public class GqlNodesQuery {

    @GraphQLField
    public static GqlNode node(DataFetchingEnvironment env,
                               @GraphQLName("id") @GraphQLNonNull String id) {

        return new GqlNode(new NodeRef(id), env.getContext());
    }

    @GraphQLField
    public static List<GqlNode> nodes(DataFetchingEnvironment env,
                                      @GraphQLName("q")
                                      @GraphQLNonNull
                                      String query,
                                      @GraphQLName("lang")
                                      @GraphQLDefaultValue(DefaultSearchLanguage.class)
                                      String lang,
                                      @GraphQLName("offset")
                                      @GraphQLDefaultValue(Defaults.IntZero.class)
                                      Integer offset,
                                      @GraphQLName("first")
                                      @GraphQLDefaultValue(Defaults.IntZero.class)
                                      Integer first) {

        SearchParameters parameters = new SearchParameters();
        parameters.setQuery(query);
        parameters.setLanguage(lang);
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        if (first > 0) {
            parameters.setLimitBy(LimitBy.FINAL_SIZE);
            parameters.setLimit(first);
        }

        if (offset > 0) {
            parameters.setSkipCount(offset);
        }

        GqlContext context = env.getContext();

        ResultSet resultSet = context.getSearchService()
                                     .query(parameters);
        try {
            return resultSet.getNodeRefs()
                            .stream()
                            .map(context::getNode)
                            .collect(Collectors.toList());
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public static class DefaultSearchLanguage implements Supplier<Object> {
        @Override
        public Object get() {
            return SearchService.LANGUAGE_FTS_ALFRESCO;
        }
    }
}
