package ru.citeck.ecos.graphql.node;

import graphql.Scalars;
import graphql.schema.*;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlTypeDefinition;

@Component
public class GqlQNameTypeDef implements GqlTypeDefinition {

    public static final String TYPE_NAME = "GqlQName";

    public static GraphQLTypeReference typeRef() {
        return new GraphQLTypeReference(TYPE_NAME);
    }

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(TYPE_NAME)
                .description("Alfresco graphql qname")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("prefix")
                        .dataFetcher(this::getPrefix)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("classTitle")
                        .dataFetcher(this::getClassTitle)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("namespace")
                        .dataFetcher(this::getNamespace)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("localName")
                        .dataFetcher(this::getLocalName)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("fullName")
                        .dataFetcher(this::getFullName)
                        .type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("shortName")
                        .dataFetcher(this::getShortName)
                        .type(Scalars.GraphQLString))
                .build();
    }

    private String getPrefix(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.prefix();
    }

    private String getClassTitle(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.classTitle();
    }

    private String getNamespace(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.namespace();
    }

    private String getLocalName(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.localName();
    }

    private String getFullName(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.fullName();
    }

    private String getShortName(DataFetchingEnvironment env) {
        GqlQName qName = env.getSource();
        return qName.shortName();
    }
}
