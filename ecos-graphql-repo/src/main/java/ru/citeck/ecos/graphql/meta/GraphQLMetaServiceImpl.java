package ru.citeck.ecos.graphql.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import graphql.ExecutionResult;
import org.alfresco.util.GUID;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GraphQLMetaServiceImpl implements GraphQLMetaService {

    private static final String META_QUERY_KEY = "gqlMeta";
    private static final String META_QUERY_TEMPLATE = "{" + META_QUERY_KEY + "{%s}}";

    private Pattern ID_FIELD_NAME_PATTERN = Pattern.compile(".*\\b([\\S]+)\\s*:\\s*id\\s.*");

    private ConvertersProvider convertersProvider = new ConvertersProvider();
    private ObjectMapper objectMapper = new ObjectMapper();

    private GraphQLService graphQLService;

    @Override
    public List<ObjectNode> getEmpty(List<?> ids, String schema) {

        Matcher idMatcher = ID_FIELD_NAME_PATTERN.matcher(schema);

        String idFieldName;
        if (idMatcher.matches()) {
            idFieldName = idMatcher.group(1);
        } else {
            idFieldName = "id";
        }

        return ids.stream().map(id -> {
            ObjectNode recordNode = JsonNodeFactory.instance.objectNode();
            recordNode.put(idFieldName, id.toString());
            return recordNode;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ObjectNode> getMeta(List<?> values, String schema) {

        String query = String.format(META_QUERY_TEMPLATE, schema);

        GqlContext context = graphQLService.getGqlContext();
        context.setMetaValues(values);

        ExecutionResult result = graphQLService.execute(query, null, context);
        return convertMeta(result, values, schema);
    }

    @Override
    public String createSchema(Class<?> metaClass) {
        MetaConverter<?> converter = convertersProvider.getConverter(metaClass);
        return converter.appendQuery(new StringBuilder()).toString();
    }

    @Override
    public <V> List<V> convertMeta(List<ObjectNode> meta, Class<V> metaClass) {

        MetaConverter<V> converter = convertersProvider.getConverter(metaClass);

        return meta.stream().map(m -> {
            try {
                return converter.convert(m);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Meta receiving error", e);
            }
        }).collect(Collectors.toList());
    }

    private List<ObjectNode> convertMeta(ExecutionResult executionResult,
                                         List<?> metaValues,
                                         String schema) {

        List<ObjectNode> result = new ArrayList<>();

        if (executionResult == null || executionResult.getData() == null) {

            result = getEmpty(metaValues.stream()
                                        .map(this::getValueId)
                                        .map(v -> v.orElse(GUID.generate()))
                                        .collect(Collectors.toList()), schema);

        } else {

            JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());
            JsonNode meta = jsonNode.get(META_QUERY_KEY);

            for (int i = 0; i < meta.size(); i++) {
                JsonNode metaNode = meta.get(i);
                result.add(metaNode instanceof NullNode ? null : (ObjectNode) metaNode);
            }
        }
        return result;
    }

    private Optional<String> getValueId(Object value) {
        if (value instanceof MetaValue) {
            return Optional.ofNullable(((MetaValue) value).getId());
        }
        try {
            Object id = PropertyUtils.getProperty(value, "id");
            if (id == null) {
                return Optional.empty();
            }
            return Optional.of(id.toString());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    @Autowired
    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
}

