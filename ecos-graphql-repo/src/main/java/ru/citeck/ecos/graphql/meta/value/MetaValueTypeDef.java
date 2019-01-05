package ru.citeck.ecos.graphql.meta.value;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.CustomGqlScalars;
import ru.citeck.ecos.graphql.GqlTypeDefinition;

import java.util.Collections;
import java.util.List;

@Component
public class MetaValueTypeDef implements GqlTypeDefinition {

    public static final String TYPE_NAME = "MetaValue";

    public static GraphQLTypeReference typeRef() {
        return new GraphQLTypeReference(TYPE_NAME);
    }

    @Override
    public GraphQLObjectType getType() {

        return GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Meta value")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("id")
                        .description("Identifier")
                        .dataFetcher(this::getId)
                        .type(Scalars.GraphQLID))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("att")
                        .description("Attribute")
                        .dataFetcher(this::getAtt)
                        .argument(GraphQLArgument.newArgument()
                                .name("n")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("edge")
                        .description("Attribute edge with name and value")
                        .dataFetcher(this::getAtt)
                        .argument(GraphQLArgument.newArgument()
                                .name("n")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("atts")
                        .description("Attributes")
                        .dataFetcher(this::getAtts)
                        .argument(GraphQLArgument.newArgument()
                                .name("n")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(GraphQLList.list(typeRef())))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("edge")
                        .description("Attribute edge")
                        .dataFetcher(this::getEdge)
                        .argument(GraphQLArgument.newArgument()
                                .name("n")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(MetaEdgeTypeDef.typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("as")
                        .description("Cast to another type")
                        .dataFetcher(this::getAs)
                        .argument(GraphQLArgument.newArgument()
                                .name("t")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("str")
                        .description("String representation")
                        .dataFetcher(this::getStr)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("json")
                        .description("Json representation")
                        .dataFetcher(this::getJson)
                        .type(CustomGqlScalars.JSON_NODE))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("num")
                        .description("Number representation")
                        .dataFetcher(this::getNum)
                        .type(Scalars.GraphQLFloat))
                .build();
    }

    private MetaValue getAs(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        String type = getParameter(env, "t");
        return value.getAs(type, env.getContext());
    }

    private String getId(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        return value.getId(env.getContext());
    }

    private MetaValue getAtt(DataFetchingEnvironment env) {
        return getAtts(env).stream().findFirst().orElse(null);
    }

    private List<MetaValue> getAtts(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        String name = getParameter(env, "n");
        List<MetaValue> result = value.getAttribute(name, env.getContext());
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    private String getStr(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        return value.getString(env.getContext());
    }

    private JsonNode getJson(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        return value.getJson(env.getContext());
    }

    private Double getNum(DataFetchingEnvironment env) {
        MetaValue value = env.getSource();
        return value.getDouble(env.getContext());
    }

    private MetaEdge getEdge(DataFetchingEnvironment env) {
        String name = env.getArgument("n");
        MetaValue value = env.getSource();
        return new MetaEdge(name, value);
    }

    private String getParameter(DataFetchingEnvironment env, String name) {
        String value = env.getArgument(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " is a mandatory parameter!");
        }
        return value;
    }
}
