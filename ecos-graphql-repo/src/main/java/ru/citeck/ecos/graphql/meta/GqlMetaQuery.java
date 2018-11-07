package ru.citeck.ecos.graphql.meta;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.DataFetchingEnvironment;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.List;

@GraphQLQueryDefinition
public class GqlMetaQuery {

    @GraphQLField
    public static List<MetaValue> gqlMeta(DataFetchingEnvironment env) {
        GqlContext context = env.getContext();
        return context.getMetaValues();
    }
}
