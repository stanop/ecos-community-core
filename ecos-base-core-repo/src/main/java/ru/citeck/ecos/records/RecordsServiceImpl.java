package ru.citeck.ecos.records;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.meta.AttributesSchema;
import ru.citeck.ecos.records.meta.RecordsMetaService;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.records.source.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecordsServiceImpl implements RecordsService {

    private static final String DEBUG_QUERY_TIME = "queryTimeMs";
    private static final String DEBUG_RECORDS_QUERY_TIME = "recordsQueryTimeMs";
    private static final String DEBUG_META_QUERY_TIME = "metaQueryTimeMs";
    private static final String DEBUG_META_SCHEMA = "schema";

    private static final Log logger = LogFactory.getLog(RecordsServiceImpl.class);

    private Map<String, RecordsMetaDAO> metaDAO = new ConcurrentHashMap<>();
    private Map<String, RecordsQueryDAO> queryDAO = new ConcurrentHashMap<>();
    private Map<String, MutableRecordsDAO> mutableDAO = new ConcurrentHashMap<>();
    private Map<String, RecordsWithMetaDAO> withMetaDAO = new ConcurrentHashMap<>();
    private Map<String, RecordsDefinitionDAO> definitionDAO = new ConcurrentHashMap<>();
    private Map<String, RecordsActionExecutor> actionExecutors = new ConcurrentHashMap<>();

    private RecordsMetaService recordsMetaService;

    @Autowired
    public RecordsServiceImpl(RecordsMetaService recordsMetaService) {
        this.recordsMetaService = recordsMetaService;
    }

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {
        Optional<RecordsQueryDAO> recordsQueryDAO = getRecordsDAO(query.getSourceId(), queryDAO);
        return recordsQueryDAO.isPresent() ? recordsQueryDAO.get().getRecords(query) : new RecordsQueryResult<>();
    }

    @Override
    public <T> RecordsQueryResult<T> getRecords(RecordsQuery query, Class<T> metaClass) {

        Map<String, String> attributes = recordsMetaService.getAttributes(metaClass);
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("Meta class doesn't has any fields with setter. Class: " + metaClass);
        }

        RecordsQueryResult<RecordMeta> meta = getRecords(query, attributes);

        return new RecordsQueryResult<>(meta, m -> recordsMetaService.instantiateMeta(metaClass, m));
    }

    @Override
    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, Map<String, String> attributes) {

        AttributesSchema schema = recordsMetaService.createSchema(attributes);
        RecordsQueryResult<RecordMeta> records = getRecords(query, schema.getSchema());
        records.setRecords(recordsMetaService.convertToFlatMeta(records.getRecords(), schema));

        return records;
    }

    @Override
    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query,
                                                     Collection<String> attributes) {
        return getRecords(query, toAttributesMap(attributes));
    }

    @Override
    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, String schema) {

        Optional<RecordsWithMetaDAO> recordsDAO = getRecordsDAO(query.getSourceId(), withMetaDAO);
        RecordsQueryResult<RecordMeta> records;

        if (recordsDAO.isPresent()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Start records with meta query: " + query.getQuery() + "\n" + schema);
            }

            long queryStart = System.currentTimeMillis();
            records = recordsDAO.get().getRecords(query, schema);
            long queryDuration = System.currentTimeMillis() - queryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop records with meta query. Duration: " + queryDuration);
            }

            if (query.isDebug()) {
                records.setDebugInfo(getClass(), DEBUG_QUERY_TIME, queryDuration);
            }

        } else  {

            Optional<RecordsQueryDAO> recordsQueryDAO = getRecordsDAO(query.getSourceId(), queryDAO);

            if (!recordsQueryDAO.isPresent()) {

                records = new RecordsQueryResult<>();

            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("Start records query: " + query.getQuery());
                }

                long recordsQueryStart = System.currentTimeMillis();
                RecordsQueryResult<RecordRef> recordRefs = recordsQueryDAO.get().getRecords(query);
                long recordsTime = System.currentTimeMillis() - recordsQueryStart;

                if (logger.isDebugEnabled()) {
                    int found = recordRefs.getRecords().size();
                    logger.debug("Stop records query. Found: " + found + "Duration: " + recordsTime);
                    logger.debug("Start meta query: " + schema);
                }

                records = new RecordsQueryResult<>();
                records.merge(recordRefs);
                records.setTotalCount(recordRefs.getTotalCount());
                records.setHasMore(recordRefs.getHasMore());

                long metaQueryStart = System.currentTimeMillis();
                records.merge(getMeta(recordRefs.getRecords(), schema));
                long metaTime = System.currentTimeMillis() - metaQueryStart;

                if (logger.isDebugEnabled()) {
                    logger.debug("Stop meta query. Duration: " + metaTime);
                }

                if (query.isDebug()) {
                    records.setDebugInfo(getClass(), DEBUG_RECORDS_QUERY_TIME, recordsTime);
                    records.setDebugInfo(getClass(), DEBUG_META_QUERY_TIME, metaTime);
                }
            }
        }

        if (query.isDebug()) {
            records.setDebugInfo(getClass(), DEBUG_META_SCHEMA, schema);
        }

        return records;
    }

    @Override
    public <T> RecordsResult<T> getMeta(List<RecordRef> records, Class<T> metaClass) {

        Map<String, String> attributes = recordsMetaService.getAttributes(metaClass);
        RecordsResult<RecordMeta> meta = getMeta(records, attributes);

        return new RecordsResult<>(meta, m -> recordsMetaService.instantiateMeta(metaClass, m));
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(Collection<RecordRef> records,
                                             Collection<String> attributes) {

        return getMeta(new ArrayList<>(records), attributes);
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records,
                                             Collection<String> attributes) {
        return getMeta(records, toAttributesMap(attributes));
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(Collection<RecordRef> records,
                                             Map<String, String> attributes) {

        return getMeta(new ArrayList<>(records), attributes);
    }

    @Override
    public <T> RecordsResult<T> getMeta(Collection<RecordRef> records,
                                        Class<T> metaClass) {

        return getMeta(new ArrayList<>(records), metaClass);
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records,
                                             Map<String, String> attributes) {

        AttributesSchema schema = recordsMetaService.createSchema(attributes);
        RecordsResult<RecordMeta> meta = getMeta(records, schema.getSchema());
        meta.setRecords(recordsMetaService.convertToFlatMeta(meta.getRecords(), schema));

        return meta;
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records, String schema) {

        RecordsResult<RecordMeta> results = new RecordsResult<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, recs) -> {

            Optional<RecordsMetaDAO> recordsDAO = getRecordsDAO(sourceId, metaDAO);
            RecordsResult<RecordMeta> meta;

            if (recordsDAO.isPresent()) {

                meta = recordsDAO.get().getMeta(records, schema);

            } else {

                meta = new RecordsResult<>();
                meta.setRecords(recs.stream().map(RecordMeta::new).collect(Collectors.toList()));
                logger.error("Records source " + sourceId + " can't return attributes");
            }

            results.merge(meta);
        });

        return results;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {
        return needRecordsDAO(mutation.getSourceId(), mutableDAO).mutate(mutation);
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {

        RecordsDelResult result = new RecordsDelResult();

        RecordsUtils.groupRefBySource(deletion.getRecords()).forEach((sourceId, sourceRecords) -> {
            MutableRecordsDAO source = needRecordsDAO(sourceId, mutableDAO);
            result.merge(source.delete(deletion));
        });

        return result;
    }

    @Override
    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records,
                                                  GroupActionConfig processConfig) {

        ActionResults<RecordRef> results = new ActionResults<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, refs) -> {

            Optional<RecordsActionExecutor> source = getRecordsDAO(sourceId, actionExecutors);

            if (source.isPresent()) {

                results.merge(source.get().executeAction(refs, processConfig));

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
    public Iterable<RecordRef> getIterableRecords(RecordsQuery query) {
        return new IterableRecords(this, query);
    }

    @Override
    public void register(RecordsDAO recordsSource) {

        if (recordsSource instanceof RecordsMetaDAO) {
            metaDAO.put(recordsSource.getId(), (RecordsMetaDAO) recordsSource);
        }
        if (recordsSource instanceof RecordsQueryDAO) {
            queryDAO.put(recordsSource.getId(), (RecordsQueryDAO) recordsSource);
        }
        if (recordsSource instanceof MutableRecordsDAO) {
            mutableDAO.put(recordsSource.getId(), (MutableRecordsDAO) recordsSource);
        }
        if (recordsSource instanceof RecordsWithMetaDAO) {
            withMetaDAO.put(recordsSource.getId(), (RecordsWithMetaDAO) recordsSource);
        }
        if (recordsSource instanceof RecordsDefinitionDAO) {
            definitionDAO.put(recordsSource.getId(), (RecordsDefinitionDAO) recordsSource);
        }
        if (recordsSource instanceof RecordsActionExecutor) {
            actionExecutors.put(recordsSource.getId(), (RecordsActionExecutor) recordsSource);
        }
    }

    @Override
    public List<MetaAttributeDef> getAttributesDef(String sourceId, Collection<String> names) {
        RecordsDefinitionDAO recordsDAO = needRecordsDAO(sourceId, definitionDAO);
        return recordsDAO.getAttributesDef(names);
    }

    @Override
    public Optional<MetaAttributeDef> getAttributeDef(String sourceId, String name) {
        return getAttributesDef(sourceId, Collections.singletonList(name)).stream().findFirst();
    }

    private Map<String, String> toAttributesMap(Collection<String> attributes) {
        Map<String, String> attributesMap = new HashMap<>();
        for (String attribute : attributes) {
            attributesMap.put(attribute, attribute);
        }
        return attributesMap;
    }

    private <T extends RecordsDAO> Optional<T> getRecordsDAO(String sourceId, Map<String, T> registry) {
        if (sourceId == null) {
            sourceId = "";
        }
        return Optional.ofNullable(registry.get(sourceId));
    }

    private <T extends RecordsDAO> T needRecordsDAO(String sourceId, Map<String, T> registry) {
        Optional<T> source = getRecordsDAO(sourceId, registry);
        if (!source.isPresent()) {
            throw new IllegalArgumentException("RecordsDAO is not found! Id: " + sourceId);
        }
        return source.get();
    }
}
