package ru.citeck.ecos.graphql.meta.value;

import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlTypeDefinition;

import java.util.List;

@Component
public class MetaEdgeTypeDef implements GqlTypeDefinition {

    public static final String TYPE_NAME = "MetaEdge";

    public static GraphQLTypeReference typeRef() {
        return new GraphQLTypeReference(TYPE_NAME);
    }

    @Override
    public GraphQLObjectType getType() {

        return GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Meta value edge")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("name")
                        .dataFetcher(this::getName)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("val")
                        .dataFetcher(this::getValue)
                        .type(MetaValueTypeDef.typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("vals")
                        .dataFetcher(this::getValues)
                        .type(GraphQLList.list(MetaValueTypeDef.typeRef())))
                .build();
    }

    private String getName(DataFetchingEnvironment env) {
        MetaEdge edge = env.getSource();
        return edge.getName();
    }

    private MetaValue getValue(DataFetchingEnvironment env) {
        return getValues(env).stream().findFirst().orElse(null);
    }

    private List<MetaValue> getValues(DataFetchingEnvironment env) {
        MetaEdge edge = env.getSource();
        return edge.getValue();
    }
}
