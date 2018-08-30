package ru.citeck.ecos.records;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Optional;

@GraphQLQueryDefinition
public class RecordsGqlDefinition {

    public static final String RECORDS_SERVICE_ID = "recordsServiceImpl";

    @GraphQLField
    public static Optional<MetaValue> record(DataFetchingEnvironment env,
                                             @GraphQLName("source") String source,
                                             @GraphQLName("id") String id) {

        GqlContext context = env.getContext();
        RecordsService recordsService = context.getService(RECORDS_SERVICE_ID);

        return recordsService.getMetaValue(env.getContext(), source, id);
    }
}
