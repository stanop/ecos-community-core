package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;
import ru.citeck.ecos.remote.RestConnection;

import java.util.Collections;
import java.util.Map;

public interface GraphQLService {

    String QUERY_TYPE = "Query";

    default ExecutionResult execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    ExecutionResult execute(String query, Map<String, Object> variables, Object context);

    /**
     * Execute local GraphQL api
     */
    ExecutionResult execute(String query, Map<String, Object> variables);

    /**
     * Execute remote GraphQL api
     */
    ExecutionResult execute(RestConnection restConn, String uri, String query, Map<String, Object> variables);


    GqlContext getGqlContext();
}
