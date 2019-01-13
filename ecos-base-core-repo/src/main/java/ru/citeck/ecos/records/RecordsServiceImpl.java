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

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();

    private RecordsMetaService recordsMetaService;

    @Autowired
    public RecordsServiceImpl(RecordsMetaService recordsMetaService) {
        this.recordsMetaService = recordsMetaService;
    }

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {
        return needRecordsSource(query.getSourceId()).getRecords(query);
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

        RecordsDAO recordsDAO = needRecordsSource(query.getSourceId());
        RecordsQueryResult<RecordMeta> records;

        if (recordsDAO instanceof RecordsWithMetaDAO) {

            RecordsWithMetaDAO recordsWithMetaDAO = (RecordsWithMetaDAO) recordsDAO;

            if (logger.isDebugEnabled()) {
                logger.debug("Start records with meta query: " + query.getQuery() + "\n" + schema);
            }

            long queryStart = System.currentTimeMillis();
            records = recordsWithMetaDAO.getRecords(query, schema);
            long queryDuration = System.currentTimeMillis() - queryStart;

            if (logger.isDebugEnabled()) {
                logger.debug("Stop records with meta query. Duration: " + queryDuration);
            }

            if (query.isDebug()) {
                records.setDebugInfo(getClass(), DEBUG_QUERY_TIME, queryDuration);
            }

        } else  {

            if (logger.isDebugEnabled()) {
                logger.debug("Start records query: " + query.getQuery());
            }

            long recordsQueryStart = System.currentTimeMillis();
            RecordsQueryResult<RecordRef> recordRefs = recordsDAO.getRecords(query);
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

            RecordsDAO recordsDAO = needRecordsSource(sourceId);
            RecordsResult<RecordMeta> meta;

            if (recordsDAO instanceof RecordsMetaDAO) {

                meta = ((RecordsMetaDAO) recordsDAO).getMeta(records, schema);

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
    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records,
                                                  GroupActionConfig processConfig) {

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
    public Iterable<RecordRef> getIterableRecords(RecordsQuery query) {
        return new IterableRecords(this, query);
    }

    @Override
    public void register(RecordsDAO recordsSource) {
        sources.put(recordsSource.getId(), recordsSource);
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

    private Map<String, String> toAttributesMap(Collection<String> attributes) {
        Map<String, String> attributesMap = new HashMap<>();
        for (String attribute : attributes) {
            attributesMap.put(attribute, attribute);
        }
        return attributesMap;
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
}
