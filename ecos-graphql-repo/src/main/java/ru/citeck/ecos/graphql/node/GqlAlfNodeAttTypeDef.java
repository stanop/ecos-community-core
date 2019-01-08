package ru.citeck.ecos.graphql.node;

import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlTypeDefinition;

import java.util.List;

@Component
public class GqlAlfNodeAttTypeDef implements GqlTypeDefinition {

    public static final String TYPE_NAME = "GqlAlfNodeAttribute";

    public static GraphQLTypeReference typeRef() {
        return new GraphQLTypeReference(TYPE_NAME);
    }

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Alfresco graphql node attribute")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("type")
                        .dataFetcher(this::getAttType)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("name")
                        .dataFetcher(this::getName)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("value")
                        .dataFetcher(this::getValue)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("values")
                        .dataFetcher(this::getValues)
                        .type(GraphQLList.list(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("qnames")
                        .dataFetcher(this::getQNames)
                        .type(GraphQLList.list(GqlQNameTypeDef.typeRef())))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("qname")
                        .dataFetcher(this::getQName)
                        .type(GqlQNameTypeDef.typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("nodes")
                        .dataFetcher(this::getNodes)
                        .type(GraphQLList.list(GqlAlfNodeTypeDef.typeRef())))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("node")
                        .dataFetcher(this::getNode)
                        .type(GqlAlfNodeTypeDef.typeRef()))
                .build();
    }

    private String getAttType(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.type().toString();
    }

    public String getName(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.name();
    }

    public String getValue(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.value().orElse(null);
    }

    public List<String> getValues(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.values();
    }

    public List<GqlQName> getQNames(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.qnames();
    }

    public GqlQName getQName(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.qname().orElse(null);
    }

    public List<GqlAlfNode> getNodes(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.nodes();
    }

    public GqlAlfNode getNode(DataFetchingEnvironment env) {
        Attribute attribute = env.getSource();
        return attribute.node().orElse(null);
    }
}
