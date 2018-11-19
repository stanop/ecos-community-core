package ru.citeck.ecos.records;

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
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class RecordsServiceImpl implements RecordsService {

    private static final String DEBUG_QUERY_TIME = "queryTimeMs";
    private static final String DEBUG_RECORDS_QUERY_TIME = "recordsQueryTimeMs";
    private static final String DEBUG_META_QUERY_TIME = "metaQueryTimeMs";

    private static final Log logger = LogFactory.getLog(RecordsService.class);

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

        RecordsDAO recordsDAO = needRecordsSource(query.getSourceId());

        if (recordsDAO instanceof RecordsWithMetaDAO) {

            RecordsWithMetaDAO recordsWithMetaDAO = (RecordsWithMetaDAO) recordsDAO;

            if (logger.isDebugEnabled()) {
                logger.debug("Start records with meta query: " + query.getQuery() + "\n" + metaSchema);
            }

            long queryStart = System.currentTimeMillis();
            RecordsResult<ObjectNode> records = recordsWithMetaDAO.getRecords(query, metaSchema);
            long queryDuration = System.currentTimeMillis() - queryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop records with meta query. Duration: " + queryDuration);
            }

            if (query.isDebug()) {
                records.setDebugInfo(getClass(), DEBUG_QUERY_TIME, queryDuration);
            }

            return records;

        } else {

            if (logger.isDebugEnabled()) {
                logger.debug("Start records query: " + query.getQuery());
            }

            long recordsQueryStart = System.currentTimeMillis();
            RecordsResult<RecordRef> records = recordsDAO.getRecords(query);
            long recordsTime = System.currentTimeMillis() - recordsQueryStart;

            if (logger.isDebugEnabled()) {
                int found = records.getRecords().size();
                logger.debug("Stop records query. Found: " + found + "Duration: " + recordsTime);
                logger.debug("Start meta query: " + metaSchema);
            }

            long metaQueryStart = System.currentTimeMillis();
            List<ObjectNode> meta = getMeta(records.getRecords(), metaSchema);
            long metaTime = System.currentTimeMillis() - metaQueryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop meta query. Duration: " + metaTime);
            }

            RecordsResult<ObjectNode> recordsWithMeta = new RecordsResult<>();
            recordsWithMeta.setHasMore(records.getHasMore());
            recordsWithMeta.setTotalCount(records.getTotalCount());
            recordsWithMeta.setDebug(records.getDebug());
            recordsWithMeta.setRecords(meta);

            if (query.isDebug()) {
                recordsWithMeta.setDebugInfo(getClass(), DEBUG_RECORDS_QUERY_TIME, recordsTime);
                recordsWithMeta.setDebugInfo(getClass(), DEBUG_META_QUERY_TIME, metaTime);
            }

            return recordsWithMeta;
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
    public Optional<MetaValueTypeDef> getTypeDefinition(String sourceId, String name) {
        return getTypesDefinition(sourceId, Collections.singletonList(name)).stream().findFirst();
    }

    @Override
    public List<MetaValueTypeDef> getTypesDefinition(String sourceId, Collection<String> names) {
        RecordsDAO recordsDAO = needRecordsSource(sourceId);
        if (recordsDAO instanceof RecordsDefinitionDAO) {
            RecordsDefinitionDAO definitionDAO = (RecordsDefinitionDAO) recordsDAO;
            return definitionDAO.getTypesDefinition(names);
        }
        return Collections.emptyList();
    }

    @Override
    public List<MetaAttributeDef> getAttsDefinition(String sourceId, Collection<String> names) {
        RecordsDAO recordsDAO = needRecordsSource(sourceId);
        if (recordsDAO instanceof RecordsDefinitionDAO) {
            RecordsDefinitionDAO definitionDAO = (RecordsDefinitionDAO) recordsDAO;
            return definitionDAO.getAttsDefinition(names);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<MetaAttributeDef> getAttDefinition(String sourceId, String name) {
        return getAttsDefinition(sourceId, Collections.singletonList(name)).stream().findFirst();
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
