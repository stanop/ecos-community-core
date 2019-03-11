package ru.citeck.ecos.graphql.node;

import graphql.Scalars;
import graphql.schema.*;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.graphql.GqlTypeDefinition;
import ru.citeck.ecos.graphql.GraphQLService;

import java.util.List;

@Component
public class GqlNodesQueryDef implements GqlTypeDefinition {

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(GraphQLService.QUERY_TYPE)
                .description("Alfresco nodes")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("nodes")
                        .dataFetcher(this::nodes)
                        .type(GraphQLList.list(GqlAlfNodeTypeDef.typeRef()))
                        .argument(GraphQLArgument.newArgument()
                                .name("q")
                                .type(Scalars.GraphQLString)
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("lang")
                                .type(Scalars.GraphQLString)
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("offset")
                                .type(Scalars.GraphQLInt)
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("first")
                                .type(Scalars.GraphQLInt)
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("consistency")
                                .type(Scalars.GraphQLString)
                                .build())
                        .build())
                .build();
    }


    private List<GqlAlfNode> nodes(DataFetchingEnvironment env) {

        String query = env.getArgument("q");
        String lang = env.getArgument("lang");
        Integer offset = env.getArgument("offset");
        Integer first = env.getArgument("first");
        String consistency = env.getArgument("consistency");

        SearchParameters parameters = new SearchParameters();
        parameters.setQuery(query);
        parameters.setLanguage(lang);
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        if (consistency != null) {
            parameters.setQueryConsistency(QueryConsistency.valueOf(consistency));
        }

        if (first != null) {
            parameters.setLimitBy(LimitBy.FINAL_SIZE);
            parameters.setLimit(first);
        }

        if (offset != null) {
            parameters.setSkipCount(offset);
        }

        AlfGqlContext context = env.getContext();

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
}
