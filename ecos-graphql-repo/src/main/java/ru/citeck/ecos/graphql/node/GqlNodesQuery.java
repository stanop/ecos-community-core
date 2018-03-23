package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.*;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;

import java.util.List;
import java.util.function.Supplier;

@GraphQLQueryDefinition
public class GqlNodesQuery {

    @GraphQLField
    public static GqlAlfNode node(DataFetchingEnvironment env,
                                  @GraphQLName("id") @GraphQLNonNull String id) {

        return new GqlAlfNode(new NodeRef(id), env.getContext());
    }

    @GraphQLField
    public static List<GqlAlfNode> nodes(DataFetchingEnvironment env,
                                         @GraphQLName("q")
                                         @GraphQLNonNull
                                         String query,
                                         @GraphQLName("lang")
                                         @GraphQLDefaultValue(DefaultSearchLanguage.class)
                                         String lang,
                                         @GraphQLName("offset")
                                         Integer offset,
                                         @GraphQLName("first")
                                         Integer first,
                                         @GraphQLName("consistency")
                                         QueryConsistency consistency) {

        SearchParameters parameters = new SearchParameters();
        parameters.setQuery(query);
        parameters.setLanguage(lang);
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        if (consistency != null) {
            parameters.setQueryConsistency(consistency);
        }

        if (first != null) {
            parameters.setLimitBy(LimitBy.FINAL_SIZE);
            parameters.setLimit(first);
        }

        if (offset != null) {
            parameters.setSkipCount(offset);
        }

        GqlContext context = env.getContext();

        ResultSet resultSet = context.getSearchService()
                                     .query(parameters);
        try {
            return context.getNodes(resultSet.getNodeRefs());
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
