package ru.citeck.ecos.graphql.node;

import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlTypeDefinition;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GqlAlfNodeTypeDef implements GqlTypeDefinition {

    public static final String TYPE_NAME = "GqlAlfNode";

    public static GraphQLTypeReference typeRef() {
        return new GraphQLTypeReference(TYPE_NAME);
    }

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Alfresco graphql node")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("displayName")
                        .description("Display name")
                        .dataFetcher(this::displayName)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("parent")
                        .description("Parent node")
                        .dataFetcher(this::parent)
                        .type(typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("aspects")
                        .description("Node aspects")
                        .dataFetcher(this::aspects)
                        .type(GraphQLList.list(GqlQNameTypeDef.typeRef())))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("aspectNames")
                        .description("Aspect names")
                        .dataFetcher(this::aspectNames)
                        .type(GraphQLList.list(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("nodeRef")
                        .description("Node reference")
                        .dataFetcher(this::nodeRef)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("type")
                        .description("Node type")
                        .dataFetcher(this::type)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("typeQName")
                        .description("Type QName")
                        .dataFetcher(this::typeQName)
                        .type(GqlQNameTypeDef.typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("isContainer")
                        .description("Is node container or not")
                        .dataFetcher(this::isContainer)
                        .type(Scalars.GraphQLBoolean))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("isDocument")
                        .description("Is node document or not")
                        .dataFetcher(this::isDocument)
                        .type(Scalars.GraphQLBoolean))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("attribute")
                        .description("Node attribute")
                        .dataFetcher(this::attribute)
                        .argument(GraphQLArgument.newArgument()
                                .name("name")
                                .type(Scalars.GraphQLString)
                                .build())
                        .type(GqlAlfNodeAttTypeDef.typeRef()))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("attributes")
                        .description("Node attributes")
                        .dataFetcher(this::attributes)
                        .argument(GraphQLArgument.newArgument()
                                .name("types")
                                .type(GraphQLList.list(Scalars.GraphQLString))
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("names")
                                .type(GraphQLList.list(Scalars.GraphQLString))
                                .build())
                        .type(GraphQLList.list(GqlAlfNodeAttTypeDef.typeRef())))
                .build();
    }


    private String displayName(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.displayName();
    }

    private GqlAlfNode parent(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.parent();
    }

    private List<GqlQName> aspects(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.aspects();
    }

    private List<String> aspectNames(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.aspectNames();
    }

    private String nodeRef(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.nodeRef();
    }

    private String type(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.type();
    }

    private GqlQName typeQName(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.typeQName().orElse(null);
    }

    private boolean isContainer(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.isContainer();
    }

    private boolean isDocument(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        return node.isDocument();
    }

    private List<Attribute> attributes(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        List<String> names = env.getArgument("names");
        List<String> types = env.getArgument("types");
        return node.attributes(types.stream().map(Attribute.Type::valueOf).collect(Collectors.toList()), names);
    }

    private Attribute attribute(DataFetchingEnvironment env) {
        GqlAlfNode node = env.getSource();
        String name = env.getArgument("name");
        return node.attribute(name);
    }
}
