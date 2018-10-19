package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaIdValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class RecordsServiceImpl implements RecordsService {

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();
    private GqlMetaUtils metaUtils;

    @Autowired
    public RecordsServiceImpl(GqlMetaUtils metaUtils) {
        this.metaUtils = metaUtils;
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
        List<ObjectNode> meta = getMeta(records, metaUtils.createSchema(metaClass));
        return metaUtils.convertMeta(meta, metaClass);
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
    public List<MetaValue> getMetaValues(GqlContext context, List<RecordRef> records) {

        List<MetaValue> results = new ArrayList<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            if (source instanceof RecordsMetaValueDAO) {
                RecordsMetaValueDAO metaValueDAO = (RecordsMetaValueDAO) source;
                results.addAll(metaValueDAO.getMetaValues(context, sourceRecords));
            } else {
                results.addAll(sourceRecords.stream().map(MetaIdValue::new).collect(Collectors.toList()));
            }
        });

        return results;
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {

        RecordsDAO recordsDAO = needRecordsSource(query.getSourceId());

        if (recordsDAO instanceof RecordsWithMetaDAO) {

            RecordsWithMetaDAO recordsWithMetaDAO = (RecordsWithMetaDAO) recordsDAO;
            return recordsWithMetaDAO.getRecords(query, metaSchema);

        } else {

            RecordsResult<RecordRef> records = recordsDAO.getRecords(query);
            List<ObjectNode> meta = getMeta(records.getRecords(), metaSchema);

            RecordsResult<ObjectNode> recordsWithMeta = new RecordsResult<>();
            recordsWithMeta.setHasMore(records.getHasMore());
            recordsWithMeta.setTotalCount(records.getTotalCount());
            recordsWithMeta.setRecords(meta);

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

            String schema = metaUtils.createSchema(metaClass);
            RecordsResult<ObjectNode> records = getRecords(query, schema);

            results.setTotalCount(records.getTotalCount());
            results.setHasMore(records.getHasMore());
            results.setRecords(metaUtils.convertMeta(records.getRecords(), metaClass));
        }

        return results;
    }

    private <T> List<T> getRecordsMeta(Collection<RecordRef> records,
                                       BiFunction<RecordsDAO, List<RecordRef>, List<T>> getMeta) {

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
