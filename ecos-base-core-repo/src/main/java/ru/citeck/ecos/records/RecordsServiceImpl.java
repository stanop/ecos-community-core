package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Service
public class RecordsServiceImpl implements RecordsService {

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();

    @Autowired
    private GroupActionService groupActionService;

    @Override
    public RecordsResult getRecords(RecordsQuery query) {
        return getRecords("", query);
    }

    @Override
    public RecordsResult getRecords(String sourceId, RecordsQuery query) {
        RecordsDAO source = getSource(sourceId);
        DaoRecordsResult result = source.queryRecords(query);
        return new RecordsResult(result, id -> new RecordRef(source.getId(), id));
    }

    @Override
    public <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> dataClass) {
        return getMeta(records, (source, recs) -> source.queryMeta(recs, dataClass));
    }

    @Override
    public Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String gqlQuery) {
        return getMeta(records, (source, recs) -> source.queryMeta(recs, gqlQuery));
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String source, String id) {
        return getSource(source).getMetaValue(context, id);
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
        return getMetaValue(context, recordRef.getSourceId(), recordRef.getId());
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String source, String name) {
        return getSource(source).getAttributeInfo(name);
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(RecordRef recordRef) {
        return getAttributeInfo(recordRef.getSourceId(), recordRef.getId());
    }

    @Override
    public List<ActionResult<RecordRef>> executeAction(RecordsQuery query, String actionId, GroupActionConfig config) {
        return executeAction("", query, actionId, config);
    }

    @Override
    public List<ActionResult<RecordRef>> executeAction(String source,
                                                   RecordsQuery query,
                                                   String actionId,
                                                   GroupActionConfig config) {
        return executeAction(new IterableRecords(this, source, query), actionId, config);
    }

    @Override
    public List<ActionResult<RecordRef>> executeAction(Iterable<RecordRef> records,
                                                   String actionId,
                                                   GroupActionConfig config) {

        Class<?> actionType = groupActionService.getActionType(actionId);

        if (RecordRef.class.isAssignableFrom(actionType)) {
            return groupActionService.execute(records, actionId, config);
        } else {
            IterableRecordsMeta recordsWithMeta = new IterableRecordsMeta<>(records, this, actionType);
            List<ActionResult<?>> actionResult = groupActionService.execute(recordsWithMeta, actionId, config);

            List<ActionResult<RecordRef>> results = new ArrayList<>();
            actionResult.forEach(r -> {
                @SuppressWarnings("unchecked")
                RecordRef recordRef = recordsWithMeta.getRecordRef(r.getData());
                if (recordRef != null) {
                    results.add(new ActionResult<>(recordRef, r.getStatus()));
                }
            });

            return results;
        }
    }

    private <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records,
                                          BiFunction<RecordsDAO,
                                                     Set<String>, Map<String, T>> getMeta) {

        Map<RecordRef, T> result = new HashMap<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = getSource(sourceId);
            Map<String, T> recordsMeta = getMeta.apply(source, sourceRecords);
            recordsMeta.forEach((k, v) -> result.put(new RecordRef(source.getId(), k), v));
        });

        return result;
    }

    private RecordsDAO getSource(String sourceId) {
        RecordsDAO source = sources.get(sourceId);
        if (source == null) {
            throw new IllegalArgumentException("Records source is not found! Id: " + sourceId);
        }
        return source;
    }

    public void register(RecordsDAO recordsSource) {
        sources.put(recordsSource.getId(), recordsSource);
    }
}
