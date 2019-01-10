package ru.citeck.ecos.graphql;

import graphql.schema.GraphQLObjectType;

public interface GqlTypeDefinition {

    GraphQLObjectType getType();
}
