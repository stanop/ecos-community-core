package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.remote.RestConnection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface GraphQLService {

    default ExecutionResult execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    ExecutionResult execute(String query,
                            Map<String, Object> variables,
                            Function<GqlContext, List<MetaValue>> valuesProvider);

    /**
     * Execute local GraphQL api
     */
    ExecutionResult execute(String query, Map<String, Object> variables);

    /**
     * Execute remote GraphQL api
     */
    ExecutionResult execute(RestConnection restConn, String uri, String query, Map<String, Object> variables);

}
