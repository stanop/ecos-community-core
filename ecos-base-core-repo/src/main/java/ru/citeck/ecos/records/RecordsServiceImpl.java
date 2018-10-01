package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Service
public class RecordsServiceImpl implements RecordsService {

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();
    private GqlMetaUtils metaUtils;

    @Autowired
    public RecordsServiceImpl(GqlMetaUtils metaUtils) {
        this.metaUtils = metaUtils;
    }

    @Override
    public RecordsResult getRecords(RecordsQuery query) {
        return getRecords("", query);
    }

    @Override
    public RecordsResult getRecords(String sourceId, RecordsQuery query) {
        return needRecordsSource(sourceId).queryRecords(query);
    }

    @Override
    public Iterable<RecordRef> getIterableRecords(RecordsQuery query) {
        return getIterableRecords("", query);
    }

    @Override
    public Iterable<RecordRef> getIterableRecords(String sourceId, RecordsQuery query) {
        return new IterableRecords(this, sourceId, query);
    }

    @Override
    public <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> metaClass) {
        if (metaClass.isAssignableFrom(RecordRef.class)) {
            Map<RecordRef, T> results = new HashMap<>();
            records.forEach(r -> results.put(r, (T) r));
            return results;
        }
        if (metaClass.isAssignableFrom(NodeRef.class)) {
            Map<RecordRef, T> results = new HashMap<>();
            records.forEach(r -> results.put(r, (T) RecordsUtils.toNodeRef(r)));
            return results;
        }
        Map<RecordRef, JsonNode> meta = getMeta(records, metaUtils.createSchema(metaClass));
        return metaUtils.convertMeta(meta, metaClass);
    }

    @Override
    public Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String gqlSchema) {
        return getMeta(records, (source, recs) -> source.getMeta(recs, gqlSchema));
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
        Optional<RecordsDAO> recordsDAO = getRecordsSource(recordRef.getSourceId());
        return recordsDAO.flatMap(dao -> dao.getMetaValue(context, recordRef));
    }

    private <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records,
                                          BiFunction<RecordsDAO,
                                                     List<RecordRef>, Map<RecordRef, T>> getMeta) {

        Map<RecordRef, T> result = new HashMap<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            result.putAll(getMeta.apply(source, sourceRecords));
        });

        return result;
    }

    @Override
    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records, GroupActionConfig processConfig) {

        ActionResults<RecordRef> results = new ActionResults<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, refs) -> {
            RecordsDAO source = needRecordsSource(sourceId);
            results.merge(source.executeAction(refs, processConfig));
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
