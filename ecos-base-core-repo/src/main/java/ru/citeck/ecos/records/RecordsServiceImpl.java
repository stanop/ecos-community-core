package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionFactory;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.actions.RecordsActionFactory;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.RecordsDAO;
import ru.citeck.ecos.records.source.RecordsDAODelegate;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        RecordsDAO source = needRecordsSource(sourceId);
        DaoRecordsResult result = source.queryRecords(query);
        Function<String, RecordRef> refMapping;
        if (source instanceof RecordsDAODelegate) {
            refMapping = RecordRef::new;
        } else {
            refMapping = id -> new RecordRef(source.getId(), id);
        }
        return new RecordsResult(result, refMapping);
    }

    @Override
    public <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> dataClass) {
        if (!dataClass.isAssignableFrom(RecordRef.class)) {
            return getMeta(records, (source, recs) -> source.queryMeta(recs, dataClass));
        } else {
            Map<RecordRef, T> results = new HashMap<>();
            records.forEach(r -> results.put(r, (T) r));
            return results;
        }
    }

    @Override
    public Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String gqlQuery) {
        return getMeta(records, (source, recs) -> source.queryMeta(recs, gqlQuery));
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String source, String id) {
        return needRecordsSource(source).getMetaValue(context, id);
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
        return getMetaValue(context, recordRef.getSourceId(), recordRef.getId());
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String source, String name) {
        return needRecordsSource(source).getAttributeInfo(name);
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

        Optional<GroupActionFactory<Object>> actionFactory = groupActionService.getActionFactory(actionId);

        if (!actionFactory.isPresent()) {
            throw new IllegalArgumentException("Action " + actionId + " is not found");
        }

        GroupActionFactory<Object> factory = actionFactory.get();
        if (factory instanceof RecordsActionFactory) {

            ParameterizedType type = (ParameterizedType) factory.getClass().getGenericSuperclass();
            Class<?> metaType = (Class) type.getActualTypeArguments()[0];

            IterableRecordsMeta recordsWithMeta = new IterableRecordsMeta<>(records, this, metaType);
            List<ActionResult<RecordInfo<?>>> actionResult =
                    groupActionService.execute(recordsWithMeta, actionId, config);

            List<ActionResult<RecordRef>> results = new ArrayList<>();
            actionResult.forEach(r -> results.add(new ActionResult<>(r.getData().getRef(), r.getStatus())));

            return results;

        } else {

            return groupActionService.execute(records, actionId, config);
        }
    }

    private <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records,
                                          BiFunction<RecordsDAO,
                                                     Set<String>, Map<String, T>> getMeta) {

        Map<RecordRef, T> result = new HashMap<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            Map<String, T> recordsMeta = getMeta.apply(source, sourceRecords);
            recordsMeta.forEach((k, v) -> result.put(new RecordRef(source.getId(), k), v));
        });

        return result;
    }

    @Override
    public Optional<RecordsDAO> getRecordsSource(String sourceId) {
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
