package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GqlMetaUtils {

    private static final String META_KEY = "meta";
    private static final String QUERY_TEMPLATE = "{" + META_KEY + ":%s{%s}}";

    private ConvertersProvider convertersProvider = new ConvertersProvider();
    private ObjectMapper objectMapper = new ObjectMapper();

    public String createQuery(String queryBase, List<String> ids, String schema) {
        String baseWithId = String.format(queryBase, String.join("\",\"", ids));
        return String.format(QUERY_TEMPLATE, baseWithId, schema);
    }

    public String createQuery(String queryBase, List<String> ids, Class<?> metaClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(metaClass);
        String schema = converter.appendQuery(new StringBuilder()).toString();
        return createQuery(queryBase, ids, schema);
    }

    public Map<String, JsonNode> convertMeta(List<String> ids, ExecutionResult executionResult) {

        JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());

        Map<String, JsonNode> result = new HashMap<>();
        JsonNode meta = jsonNode.get(META_KEY);

        for (int i = 0; i < meta.size(); i++) {
            result.put(ids.get(i), meta.get(i));
        }

        return result;
    }

    public <V> Map<String, V> convertMeta(List<String> ids, ExecutionResult executionResult, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        Map<String, JsonNode> meta = convertMeta(ids, executionResult);
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

    public String createQuery(Class<?> dataClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(dataClass);
        return converter.appendQuery(new StringBuilder()).toString();
    }

    public <T> T convertData(JsonNode node, Class<T> dataClass) {
        MetaConverter<T> converter = convertersProvider.getConverter(dataClass);
        try {
            return converter.convert(node);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
