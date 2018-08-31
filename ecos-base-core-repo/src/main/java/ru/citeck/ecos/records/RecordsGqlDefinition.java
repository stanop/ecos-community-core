package ru.citeck.ecos.records;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GraphQLQueryDefinition
public class RecordsGqlDefinition {

    private static final String RECORDS_SERVICE_ID = "recordsServiceImpl";

    @GraphQLField
    public static List<MetaValue> records(DataFetchingEnvironment env,
                                          @GraphQLName("source") String source,
                                          @GraphQLName("refs") List<String> refs) {

        GqlContext context = env.getContext();
        RecordsService recordsService = context.getService(RECORDS_SERVICE_ID);

        return refs.stream()
                   .map(r -> recordsService.getMetaValue(env.getContext(), source, r))
                   .flatMap( o -> o.map(Stream::of).orElseGet(Stream::empty))
                   .collect(Collectors.toList());
    }
}
