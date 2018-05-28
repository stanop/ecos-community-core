package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;

import java.util.Collections;
import java.util.Map;

public interface GraphQLService {

    default ExecutionResult execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    ExecutionResult execute(String query, Map<String, Object> variables);

}
