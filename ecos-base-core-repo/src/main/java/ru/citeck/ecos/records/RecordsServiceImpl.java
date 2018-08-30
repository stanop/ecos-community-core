package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.RecordsDAO;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Service
public class RecordsServiceImpl implements RecordsService {

    private static final String SOURCE_ID_DEFAULT = AlfNodesRecordsDAO.ID;

    private Map<String, RecordsDAO> sources = new ConcurrentHashMap<>();

    @Autowired
    private GroupActionService groupActionService;

    @Override
    public RecordsResult getRecords(RecordsQuery query) {
        return getRecords(SOURCE_ID_DEFAULT, query);
    }

    @Override
    public RecordsResult getRecords(String sourceId, RecordsQuery query) {
        RecordsDAO source = getSource(sourceId);
        DaoRecordsResult result = source.queryRecords(query);
        return new RecordsResult(result, (id) -> createRecord(source.getId(), id));
    }

    @Override
    public <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> dataClass) {
        return getMeta(records, (source, recs) -> source.queryMeta(recs, dataClass));
    }

    @Override
    public Map<RecordRef, ObjectNode> getMeta(Collection<RecordRef> records, String gqlQuery) {
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

    private <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records,
                                          BiFunction<RecordsDAO,
                                                     Set<String>, Map<String, T>> getMeta) {

        Map<RecordRef, T> result = new HashMap<>();

        groupBySource(records).forEach((sourceId, sourceRecords) -> {
            RecordsDAO source = getSource(sourceId);
            Map<String, T> recordsMeta = getMeta.apply(source, sourceRecords);
            recordsMeta.forEach((k, v) -> result.put(createRecord(source.getId(), k), v));
        });

        return result;
    }

    private RecordRef createRecord(String source, String id) {
        if (SOURCE_ID_DEFAULT.equals(source)) {
            return new RecordRef("", id);
        } else {
            return new RecordRef(source, id);
        }
    }

    private Map<String, Set<String>> groupBySource(Collection<RecordRef> records) {
        Map<String, Set<String>> result = new HashMap<>();
        for (RecordRef record : records) {
            String sourceId = record.getSourceId();
            if (StringUtils.isBlank(sourceId)) {
                sourceId = SOURCE_ID_DEFAULT;
            }
            String recordId = record.getId();
            result.computeIfAbsent(sourceId, key -> new HashSet<>()).add(recordId);
        }
        return result;
    }

    private RecordsDAO getSource(String sourceId) {
        if (StringUtils.isBlank(sourceId)) {
            sourceId = SOURCE_ID_DEFAULT;
        }
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
