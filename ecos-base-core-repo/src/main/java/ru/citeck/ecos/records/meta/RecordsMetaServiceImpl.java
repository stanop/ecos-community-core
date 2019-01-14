package ru.citeck.ecos.records.meta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.alfresco.util.GUID;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.utils.json.ObjectKeyGenerator;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecordsMetaServiceImpl implements RecordsMetaService {

    private static final String META_QUERY_KEY = "gqlMeta";
    private static final String META_QUERY_TEMPLATE = "{" + META_QUERY_KEY + "{%s}}";

    private static final Log logger = LogFactory.getLog(RecordsMetaServiceImpl.class);

    private Map<Class<?>, ScalarField<?>> scalars = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, String>> attributesCache = new ConcurrentHashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    private GraphQLService graphQLService;
    private RecordsUtils recordsUtils;

    @Autowired
    public RecordsMetaServiceImpl(GraphQLService graphQLService,
                                  RecordsUtils recordsUtils) {
        this.graphQLService = graphQLService;
        this.recordsUtils = recordsUtils;
    }

    @PostConstruct
    public void init() {
        Arrays.asList(
                new ScalarField<>(String.class, "str"),
                new ScalarField<>(Boolean.class, "bool"),
                new ScalarField<>(boolean.class, "bool"),
                new ScalarField<>(Double.class, "num"),
                new ScalarField<>(double.class, "num"),
                new ScalarField<>(Float.class, "num"),
                new ScalarField<>(float.class, "num"),
                new ScalarField<>(Integer.class, "num"),
                new ScalarField<>(int.class, "num"),
                new ScalarField<>(Long.class, "num"),
                new ScalarField<>(long.class, "num"),
                new ScalarField<>(Short.class, "num"),
                new ScalarField<>(short.class, "num"),
                new ScalarField<>(Byte.class, "num"),
                new ScalarField<>(byte.class, "num"),
                new ScalarField<>(Date.class, "str")
        ).forEach(s -> scalars.put(s.getFieldType(), s));
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<?> records, String schema) {

        String query = String.format(META_QUERY_TEMPLATE, schema);

        GqlContext context = graphQLService.getGqlContext();
        context.setMetaValues(records);

        ExecutionResult result = graphQLService.execute(query, null, context);

        return new RecordsResult<>(convertMeta(result, records));
    }

    private List<RecordMeta> convertMeta(ExecutionResult executionResult,
                                         List<?> metaValues) {

        List<RecordMeta> result = new ArrayList<>();

        if (executionResult == null || executionResult.getData() == null) {

            result = metaValues.stream()
                               .map(v -> Optional.ofNullable(recordsUtils.getMetaValueId(v)))
                               .map(v -> new RecordMeta(v.orElse(GUID.generate())))
                               .collect(Collectors.toList());

        } else {

            JsonNode jsonNode = objectMapper.valueToTree(executionResult.getData());
            JsonNode meta = jsonNode.get(META_QUERY_KEY);

            for (int i = 0; i < meta.size(); i++) {
                RecordMeta recMeta = new RecordMeta(recordsUtils.getMetaValueId(metaValues.get(i)));
                JsonNode attributes = meta.get(i);
                if (attributes instanceof ObjectNode) {
                    recMeta.setAttributes((ObjectNode) attributes);
                }
                result.add(recMeta);
            }
        }
        return result;
    }

    @Override
    public List<RecordMeta> convertToFlatMeta(List<RecordMeta> meta, AttributesSchema schema) {
        return meta.stream()
                   .map(m -> convertToFlatMeta(m, schema))
                   .collect(Collectors.toList());
    }

    private RecordMeta convertToFlatMeta(RecordMeta meta, AttributesSchema schema) {

        ObjectNode attributes = meta.getAttributes();
        ObjectNode flatAttributes = JsonNodeFactory.instance.objectNode();
        Map<String, String> keysMapping = schema.getKeysMapping();

        Iterator<String> fields = attributes.fieldNames();

        while (fields.hasNext()) {
            String key = fields.next();
            flatAttributes.put(keysMapping.get(key), toFlatNode(attributes.get(key)));
        }

        RecordMeta recordMeta = new RecordMeta(meta.getId());
        recordMeta.setAttributes(flatAttributes);

        return recordMeta;
    }

    private JsonNode toFlatNode(JsonNode input) {

        JsonNode node = input;

        if (node.isObject() && node.size() > 1) {

            ObjectNode objNode = JsonNodeFactory.instance.objectNode();
            final JsonNode finalNode = node;

            node.fieldNames().forEachRemaining(name ->
                objNode.put(name, toFlatNode(finalNode.get(name)))
            );

            node = objNode;

        } else if (node.isObject() && node.size() == 1) {

            String fieldName = node.fieldNames().next();
            JsonNode value = node.get(fieldName);

            if ("json".equals(fieldName)) {
                node = value;
            } else {
                node = toFlatNode(value);
            }

        } else if (node.isArray()) {

            ArrayNode newArr = JsonNodeFactory.instance.arrayNode();

            for (JsonNode n : node) {
                newArr.add(toFlatNode(n));
            }

            node = newArr;
        }

        return node;
    }

    @Override
    public Map<String, String> getAttributes(Class<?> metaClass) {
        return attributesCache.computeIfAbsent(metaClass, c -> getAttributes(c, new HashSet<>()));
    }

    @Override
    public <T> T instantiateMeta(Class<T> metaClass, RecordMeta meta) {
        try {
            return objectMapper.treeToValue(meta.getAttributes(), metaClass);
        } catch (JsonProcessingException e) {
            logger.error("Error while meta instantiating", e);
            return null;
        }
    }

    @Override
    public AttributesSchema createSchema(Map<String, String> attributes) {
        return createSchema(attributes, true);
    }

    private AttributesSchema createSchema(Map<String, String> attributes, boolean generateKeys) {

        StringBuilder schema = new StringBuilder();
        ObjectKeyGenerator keys = new ObjectKeyGenerator();

        Map<String, String> keysMapping = new HashMap<>();

        attributes.forEach((name, path) -> {
            String key = generateKeys ? keys.incrementAndGet() : name;
            keysMapping.put(key, name);
            schema.append(key).append(":");
            if (path.charAt(0) == '.') {
                schema.append(path, 1, path.length());
            } else {
                schema.append(path);
            }
            schema.append(",");
        });
        schema.setLength(schema.length() - 1);

        return new AttributesSchema(schema.toString(), keysMapping);
    }

    private Map<String, String> getAttributes(Class<?> metaClass, Set<Class<?>> visited) {

        if (!visited.add(metaClass)) {
            throw new IllegalArgumentException("Recursive meta fields is not supported! " +
                                               "Class: " + metaClass + " visited: " + visited);
        }

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(metaClass);
        Map<String, String> attributes = new HashMap<>();

        StringBuilder schema = new StringBuilder();

        for (PropertyDescriptor descriptor : descriptors) {

            Method writeMethod = descriptor.getWriteMethod();

            if (writeMethod == null) {
                continue;
            }

            Class<?> propType = descriptor.getPropertyType();
            boolean isMultiple = false;

            if (List.class.isAssignableFrom(propType) || Set.class.isAssignableFrom(propType)) {
                ParameterizedType parameterType = (ParameterizedType) writeMethod.getGenericParameterTypes()[0];
                propType = (Class<?>) parameterType.getActualTypeArguments()[0];
                isMultiple = true;
            }

            String attributeSchema = getAttributeSchema(metaClass, writeMethod, descriptor.getName(), isMultiple);
            attributeSchema = attributeSchema.replaceAll("'", "\"");

            schema.setLength(0);
            char lastChar = attributeSchema.charAt(attributeSchema.length() - 1);

            if (lastChar == '}' || !attributeSchema.startsWith(".att")) {
                attributes.put(descriptor.getName(), attributeSchema);
                continue;
            }

            schema.append(attributeSchema).append("{");

            ScalarField<?> scalarField = scalars.get(propType);
            if (scalarField == null) {

                Map<String, String> propSchema = getAttributes(propType);
                schema.append(createSchema(propSchema, false).getSchema());

            } else {

                schema.append(scalarField.getSchema());
            }

            if (schema.charAt(schema.length() - 1) != '{') {

                schema.append("}");
                attributes.put(descriptor.getName(), schema.toString());

            } else {

                logger.error("Class without attributes: " + propType + " property: " + descriptor.getName());
            }
        }

        visited.remove(metaClass);

        return attributes;
    }

    private String getAttributeSchema(Class<?> scope, Method writeMethod, String fieldName, boolean multiple) {

        MetaAtt attInfo = writeMethod.getAnnotation(MetaAtt.class);

        if (attInfo == null) {
            Field field;
            try {
                field = scope.getDeclaredField(fieldName);
                if (field != null) {
                    attInfo = field.getAnnotation(MetaAtt.class);
                }
            } catch (NoSuchFieldException e) {
                //do nothing
            }
        }

        String schema;
        if (attInfo == null || attInfo.value().isEmpty()) {
            if ("id".equals(fieldName)) {
                schema = ".id";
            } else {
                schema = ".att(n:'" + fieldName + "')";
            }
        } else {
            schema = convertAttDefinition(attInfo.value(), null, multiple);
        }
        return schema.replaceAll("'", "\"");
    }

    private String convertAttDefinition(String def, String defaultScalar, boolean multiple) {

        if (def.startsWith(".")) {
            return def.substring(1);
        }

        String fieldName = def;
        String scalarField = defaultScalar;

        int questionIdx = fieldName.indexOf('?');
        if (questionIdx >= 0) {
            scalarField = fieldName.substring(questionIdx + 1);
            fieldName = fieldName.substring(0, questionIdx);
        }

        String result = (multiple ? ".atts" : ".att") + "(n:\"" + fieldName + "\")";
        if (scalarField != null) {
            return result + "{" + scalarField + "}";
        } else {
            return result;
        }
    }

    public static class ScalarField<FieldType> {

        private String schema;
        private Class<FieldType> fieldClass;

        public ScalarField(Class<FieldType> fieldClass, String schema) {
            this.schema = schema;
            this.fieldClass = fieldClass;
        }

        public String getSchema() {
            return schema;
        }

        public Class<FieldType> getFieldType() {
            return fieldClass;
        }
    }
}
