package ru.citeck.ecos.graphql.node;

import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GqlTypeDefinition;
import ru.citeck.ecos.graphql.GraphQLService;

@Component
public class GqlNodeQueryDef implements GqlTypeDefinition {

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(GraphQLService.QUERY_TYPE)
                .description("Alfresco node")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("node")
                        .dataFetcher(this::node)
                        .type(GqlAlfNodeTypeDef.typeRef())
                        .argument(GraphQLArgument.newArgument()
                                .name("id")
                                .type(Scalars.GraphQLString)
                                .build())
                        .build())
                .build();
    }


    private GqlAlfNode node(DataFetchingEnvironment env) {
        GqlContext context = env.getContext();
        String id = env.getArgument("id");
        return context.getNode(id).orElse(null);
    }
}
