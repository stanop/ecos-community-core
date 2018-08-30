package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MetaProvider {

    private GraphQLService graphQLService;

    @Autowired
    private ConvertersProvider convertersProvider;

    private ObjectMapper objectMapper = new ObjectMapper();

    public GqlQuery createQuery(String queryBase, Collection<String> ids, String schema) {

        Map<String, String> keysMapping = new HashMap<>();

        StringBuilder query = new StringBuilder("{");
        int idx = 0;
        for (String id : ids) {
            String key = "a" + idx++;
            keysMapping.put(key, id);
            query.append(key)
                    .append(":")
                    .append(String.format(queryBase, id))
                    .append("{...meta}\n");
        }
        query.append("}");

        query.append("fragment meta on MetaValue {").append(schema).append("}");

        return new GqlQuery(query.toString(), keysMapping);
    }

    public Map<String, ObjectNode> queryMeta(String queryBase, Collection<String> ids, String schema) {

        Map<String, String> idKeys = new HashMap<>();

        StringBuilder query = new StringBuilder("{");
        int idx = 0;
        for (String id : ids) {
            String key = "a" + idx++;
            idKeys.put(key, id);
            query.append(key)
                 .append(":")
                 .append(String.format(queryBase, id))
                 .append("{...dataSchema}\n");
        }
        query.append("}");

        query.append("fragment dataSchema on MetaValue {").append(schema).append("}");

        ExecutionResult gqlResult = graphQLService.execute(query.toString());

        JsonNode jsonNode = objectMapper.valueToTree(gqlResult.getData());

        Map<String, ObjectNode> result = new HashMap<>();
        idKeys.forEach((key, id) -> result.put(id, (ObjectNode) jsonNode.get(key)));

        return result;
    }

    public <V> Map<String, V> queryMeta(String queryBase, Collection<String> ids, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        String schema = converter.appendQuery(new StringBuilder()).toString();

        Map<String, ObjectNode> meta = queryMeta(queryBase, ids, schema);
        Map<String, V> result = new HashMap<>();

        meta.forEach((id, value) -> {
            try {
                result.put(id, converter.convert(value));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                result.put(id, null);
            }
        });

        return result;
    }

    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    public static class GqlQuery {

        final String query;
        final Map<String, String> keysMapping;

        public GqlQuery(String query, Map<String, String> keysMapping) {
            this.query = query;
            this.keysMapping = keysMapping;
        }
    }
}
