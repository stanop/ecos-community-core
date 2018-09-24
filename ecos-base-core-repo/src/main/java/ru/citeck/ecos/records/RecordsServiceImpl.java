package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.actions.RecordsActionFactory;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.lang.reflect.ParameterizedType;
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
        return needRecordsSource(sourceId).queryRecords(query);
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
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
        RecordsDAO recordsDAO = needRecordsSource(recordRef.getSourceId());
        return recordsDAO.getMetaValue(context, recordRef.getId());
    }

    @Override
    public ActionResults<RecordRef> executeAction(RecordsQuery query, String actionId, GroupActionConfig config) {
        return executeAction("", query, actionId, config);
    }

    @Override
    public ActionResults<RecordRef> executeAction(String source,
                                                       RecordsQuery query,
                                                       String actionId,
                                                       GroupActionConfig config) {
        RecordsQuery iterableQuery = new RecordsQuery(query);
        iterableQuery.setMaxItems(0);
        return executeAction(new IterableRecords(this, source, iterableQuery), actionId, config);
    }

    @Override
    public ActionResults<RecordRef> executeAction(Iterable<RecordRef> records,
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
            ActionResults<RecordInfo<?>> actionResult =
                    groupActionService.execute(recordsWithMeta, actionId, config);

            List<ActionResult<RecordRef>> results = new ArrayList<>();
            actionResult.getResults().forEach(r ->
                    results.add(new ActionResult<>(r.getData().getRef(), r.getStatus())));

            ActionResults<RecordRef> result = new ActionResults<>();
            result.setResults(results);
            result.setErrorsCount(actionResult.getErrorsCount());
            result.setProcessedCount(actionResult.getProcessedCount());

            return result;

        } else {

            return groupActionService.execute(records, actionId, config);
        }
    }

    private <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records,
                                          BiFunction<RecordsDAO,
                                                     Set<RecordRef>, Map<RecordRef, T>> getMeta) {

        Map<RecordRef, T> result = new HashMap<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            result.putAll(getMeta.apply(source, sourceRecords));
        });

        return result;
    }

    @Override
    public Optional<RecordsDAO> getRecordsSource(String sourceId) {
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
