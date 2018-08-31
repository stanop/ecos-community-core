package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;
import org.springframework.web.client.RestTemplate;

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
    ExecutionResult execute(RestTemplate template, String uri, String query, Map<String, Object> variables);

}
