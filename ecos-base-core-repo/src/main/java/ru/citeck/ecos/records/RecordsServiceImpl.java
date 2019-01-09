package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.graphql.meta.GraphQLMetaService;
import ru.citeck.ecos.records.request.RespRecord;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.source.*;
import ru.citeck.ecos.utils.json.ObjectKeyGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class RecordsServiceImpl implements RecordsService {

    private static final String DEBUG_QUERY_TIME = "queryTimeMs";
    private static final String DEBUG_RECORDS_QUERY_TIME = "recordsQueryTimeMs";
    private static final String DEBUG_META_QUERY_TIME = "metaQueryTimeMs";
    private static final String RECORD_ID_FIELD = "recordId";

    private static final Log logger = LogFactory.getLog(RecordsServiceImpl.class);

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();

    private GraphQLMetaService graphQLMetaService;

    @Autowired
    public RecordsServiceImpl(GraphQLMetaService graphQLMetaService) {
        this.graphQLMetaService = graphQLMetaService;
    }

    @Override
    public RecordsResult<RecordRef> getRecords(RecordsQuery query) {
        return needRecordsSource(query.getSourceId()).getRecords(query);
    }

    @Override
    public Iterable<RecordRef> getIterableRecords(RecordsQuery query) {
        return new IterableRecords(this, query);
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {
        RecordsDAO recordsDAO = needRecordsSource(mutation.getSourceId());
        if (recordsDAO instanceof MutableRecordsDAO) {
            return ((MutableRecordsDAO) recordsDAO).mutate(mutation);
        }
        throw new IllegalArgumentException("Mutate operation is not supported by " + mutation.getSourceId());
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {

        RecordsDelResult result = new RecordsDelResult();

        RecordsUtils.groupRefBySource(deletion.getRecords()).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            if (source instanceof MutableRecordsDAO) {
                result.merge(((MutableRecordsDAO) source).delete(deletion));
            } else {
                throw new IllegalArgumentException("Mutate operation is not supported by " + sourceId);
            }
        });

        return result;
    }

    @Override
    public RecordsResult<RespRecord> getRecords(RecordsQuery query, Collection<String> fields) {
        Map<String, String> fieldsMap = new HashMap<>();
        for (String field : fields) {
            fieldsMap.put(field, field);
        }
        return getRecords(query, fieldsMap);
    }

    private String getSchema(Map<String, String> attributes, Map<String, String> keysMapping) {

        ObjectKeyGenerator keys = new ObjectKeyGenerator();

        StringBuilder schemaBuilder = new StringBuilder(RECORD_ID_FIELD).append(":id\n");

        attributes.forEach((attribute, path) -> {

            String fieldName = path.replace("/", "");

            String valueField = "str";
            int questionIdx = path.indexOf('?');

            if (questionIdx >= 0) {
                fieldName = path.substring(0, questionIdx);
                valueField = path.substring(questionIdx + 1);
            }

            String queryFieldName = keys.incrementAndGet();

            keysMapping.put(queryFieldName, attribute);
            schemaBuilder.append(queryFieldName)
                    .append(":att(n:\"")
                    .append(fieldName)
                    .append("\"){")
                    .append(valueField)
                    .append("}");
        });

        return schemaBuilder.toString();
    }

    @Override
    public RecordsResult<RespRecord> getRecords(RecordsQuery query, Map<String, String> attributes) {

        Map<String, String> attributesMapping = new HashMap<>();
        String schema = getSchema(attributes, attributesMapping);

        RecordsResult<ObjectNode> records = getRecords(query, schema, true);

        return new RecordsResult<>(records, node -> {

            RespRecord result = new RespRecord();

            result.setId(new RecordRef(node.get(RECORD_ID_FIELD).asText()));

            Map<String, JsonNode> flatAttributes = result.getAttributes();
            attributesMapping.forEach((k, v) -> flatAttributes.put(v, node.get(k)));

            return result;
        });
    }

    @Override
    public List<RespRecord> getMeta(Collection<RecordRef> records, Collection<String> fields) {
        Map<String, String> fieldsMap = new HashMap<>();
        fields.forEach(field -> fieldsMap.put(field, field));
        return getMeta(records, fieldsMap);
    }

    @Override
    public List<RespRecord> getMeta(Collection<RecordRef> records, Map<String, String> attributes) {

        Map<String, String> attributesMapping = new HashMap<>();
        String schema = getSchema(attributes, attributesMapping);

        List<ObjectNode> meta = getMeta(records, schema, true);

        int idx = 0;
        return records.stream().map(recordRef -> {

            ObjectNode record = meta.get(idx);

            RespRecord result = new RespRecord();
            result.setId(new RecordRef(record.get(RECORD_ID_FIELD).asText()));

            Map<String, JsonNode> flatAttributes = result.getAttributes();
            attributesMapping.forEach((k, v) -> flatAttributes.put(v, record.get(k)));

            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> getMeta(Collection<RecordRef> records, Class<T> metaClass) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        if (metaClass.isAssignableFrom(RecordRef.class)) {
            @SuppressWarnings("unchecked")
            List<T> result = new ArrayList<>((Collection<T>) records);
            return result;
        }
        if (metaClass.isAssignableFrom(NodeRef.class)) {
            @SuppressWarnings("unchecked")
            List<T> result = (List<T>) records.stream()
                                              .map(RecordsUtils::toNodeRef)
                                              .collect(Collectors.toList());
            return result;
        }
        List<ObjectNode> meta = getMeta(records, graphQLMetaService.createSchema(metaClass));
        return graphQLMetaService.convertMeta(meta, metaClass);
    }

    @Override
    public List<ObjectNode> getMeta(Collection<RecordRef> records, String metaSchema) {
        return getMeta(records, metaSchema, false);
    }

    @Override
    public List<ObjectNode> getMeta(Collection<RecordRef> records, String metaSchema, boolean flat) {
        List<ObjectNode> meta = getRecordsMeta(records, metaSchema);
        if (flat) {
            return meta.stream().map(this::toFlatObject).collect(Collectors.toList());
        } else {
            return meta;
        }
    }

    private List<ObjectNode> getRecordsMeta(Collection<RecordRef> records, String metaSchema) {
        return getRecordsMeta(records, (source, recs) -> {
            if (source instanceof RecordsMetaDAO) {
                RecordsMetaDAO metaDAO = (RecordsMetaDAO) source;
                return metaDAO.getMeta(recs, metaSchema);
            } else {
                return recs.stream().map(r -> {
                    ObjectNode recordNode = JsonNodeFactory.instance.objectNode();
                    recordNode.set("id", TextNode.valueOf(r.toString()));
                    return recordNode;
                }).collect(Collectors.toList());
            }
        });
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {
        return getRecords(query, metaSchema, false);
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema, boolean flat) {

        RecordsDAO recordsDAO = needRecordsSource(query.getSourceId());
        RecordsResult<ObjectNode> records;

        if (recordsDAO instanceof RecordsWithMetaDAO) {

            RecordsWithMetaDAO recordsWithMetaDAO = (RecordsWithMetaDAO) recordsDAO;

            if (logger.isDebugEnabled()) {
                logger.debug("Start records with meta query: " + query.getQuery() + "\n" + metaSchema);
            }

            long queryStart = System.currentTimeMillis();
            records = recordsWithMetaDAO.getRecords(query, metaSchema);
            long queryDuration = System.currentTimeMillis() - queryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop records with meta query. Duration: " + queryDuration);
            }

            if (query.isDebug()) {
                records.setDebugInfo(getClass(), DEBUG_QUERY_TIME, queryDuration);
            }

        } else {

            if (logger.isDebugEnabled()) {
                logger.debug("Start records query: " + query.getQuery());
            }

            long recordsQueryStart = System.currentTimeMillis();
            RecordsResult<RecordRef> recordRefs = recordsDAO.getRecords(query);
            long recordsTime = System.currentTimeMillis() - recordsQueryStart;

            if (logger.isDebugEnabled()) {
                int found = recordRefs.getRecords().size();
                logger.debug("Stop records query. Found: " + found + "Duration: " + recordsTime);
                logger.debug("Start meta query: " + metaSchema);
            }

            long metaQueryStart = System.currentTimeMillis();
            List<ObjectNode> meta = getMeta(recordRefs.getRecords(), metaSchema);
            long metaTime = System.currentTimeMillis() - metaQueryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop meta query. Duration: " + metaTime);
            }

            records = new RecordsResult<>();
            records.setHasMore(recordRefs.getHasMore());
            records.setTotalCount(recordRefs.getTotalCount());
            records.setDebug(records.getDebug());
            records.setRecords(meta);

            if (query.isDebug()) {
                records.setDebugInfo(getClass(), DEBUG_RECORDS_QUERY_TIME, recordsTime);
                records.setDebugInfo(getClass(), DEBUG_META_QUERY_TIME, metaTime);
            }
        }

        if (flat) {
            return new RecordsResult<>(records, this::toFlatObject);
        } else {
            return records;
        }
    }

    @Override
    public <T> RecordsResult<T> getRecords(RecordsQuery query, Class<T> metaClass) {

        RecordsResult<T> results = new RecordsResult<>();

        if (metaClass.isAssignableFrom(RecordRef.class)
            || metaClass.isAssignableFrom(NodeRef.class)) {

            RecordsResult<RecordRef> records = getRecords(query);
            results.setTotalCount(records.getTotalCount());
            results.setHasMore(records.getHasMore());
            results.setRecords(getMeta(records.getRecords(), metaClass));

        } else {

            String schema = graphQLMetaService.createSchema(metaClass);
            RecordsResult<ObjectNode> records = getRecords(query, schema);

            results.setTotalCount(records.getTotalCount());
            results.setHasMore(records.getHasMore());
            results.setRecords(graphQLMetaService.convertMeta(records.getRecords(), metaClass));
        }

        return results;
    }

    private ObjectNode toFlatObject(ObjectNode node) {
        JsonNode result = toFlatNode(node, true);
        if (result instanceof ObjectNode) {
            return (ObjectNode) result;
        }
        Class<?> clazz = result != null ? result.getClass() : null;
        throw new IllegalStateException("toFlatNode should return ObjectNode, but found: " + clazz);
    }

    private JsonNode toFlatNode(JsonNode input, boolean isRoot) {

        JsonNode node = input;

        if (node.isObject() && (isRoot || node.size() > 1)) {

            ObjectNode objNode = JsonNodeFactory.instance.objectNode();
            final JsonNode finalNode = node;

            node.fieldNames().forEachRemaining(name ->
                objNode.put(name, toFlatNode(finalNode.get(name), false))
            );

            node = objNode;

        } else if (node.isObject() && node.size() == 1) {

            String fieldName = node.fieldNames().next();
            JsonNode value = node.get(fieldName);

            node = toFlatNode(value, false);

        } else if (node.isArray()) {

            ArrayNode newArr = JsonNodeFactory.instance.arrayNode();

            for (JsonNode n : node) {
                newArr.add(toFlatNode(n, false));
            }

            node = newArr;
        }

        return node;
    }

    private <T> List<T> getRecordsMeta(Collection<RecordRef> records,
                                       BiFunction<RecordsDAO, List<RecordRef>, List<T>> getMeta) {

        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            result.addAll(getMeta.apply(source, sourceRecords));
        });

        return result;
    }

    @Override
    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records, GroupActionConfig processConfig) {

        ActionResults<RecordRef> results = new ActionResults<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, refs) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            if (source instanceof RecordsActionExecutor) {
                RecordsActionExecutor executor = (RecordsActionExecutor) source;
                results.merge(executor.executeAction(refs, processConfig));
            } else {
                ActionStatus status = ActionStatus.skipped("RecordsDAO can't execute action");
                results.addResults(refs.stream()
                                       .map(r -> new ActionResult<>(r, status))
                                       .collect(Collectors.toList()));
            }
        });
        return results;
    }

    @Override
    public List<MetaAttributeDef> getAttributesDef(String sourceId, Collection<String> names) {
        RecordsDAO recordsDAO = needRecordsSource(sourceId);
        if (recordsDAO instanceof RecordsDefinitionDAO) {
            RecordsDefinitionDAO definitionDAO = (RecordsDefinitionDAO) recordsDAO;
            return definitionDAO.getAttributesDef(names);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<MetaAttributeDef> getAttributeDef(String sourceId, String name) {
        return getAttributesDef(sourceId, Collections.singletonList(name)).stream().findFirst();
    }

    private Optional<RecordsDAO> getRecordsSource(String sourceId) {
        if (sourceId == null) {
            sourceId = "";
        }
        return Optional.ofNullable(sources.get(sourceId));
    }

    private RecordsDAO needRecordsSource(String sourceId) {
        Optional<RecordsDAO> source = getRecordsSource(sourceId);
        if (!source.isPresent()) {
            throw new IllegalArgumentException("Records source is not found! Id: " + sourceId);
        }
        return source.get();
    }

    public void register(RecordsDAO recordsSource) {
        sources.put(recordsSource.getId(), recordsSource);
    }
}
