package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;
import ru.citeck.ecos.remote.RestConnection;

import java.util.Collections;
import java.util.Map;

public interface GraphQLService {

    default ExecutionResult execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    /**
     * Execute local GraphQL api
     */
    ExecutionResult execute(String query, Map<String, Object> variables);

    /**
     * Execute remote GraphQL api
     */
    ExecutionResult execute(RestConnection restConn, String uri, String query, Map<String, Object> variables);

}
