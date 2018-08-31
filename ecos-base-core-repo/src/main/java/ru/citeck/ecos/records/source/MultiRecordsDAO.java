package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.impl.ConvertGroupAction;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.action.MultiSourceGroupAction;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author Pavel Simonov
 */
public class MultiRecordsDAO extends AbstractRecordsDAO {

    private List<RecordsDAO> recordsDao;
    private Map<String, RecordsDAO> daoBySource = new ConcurrentHashMap<>();

    public MultiRecordsDAO(String id) {
        super(id);
    }

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {

        DaoRecordsResult result = new DaoRecordsResult(query);

        RecordsQuery localQuery = new RecordsQuery(query);

        int sourceIdx = 0;
        String afterId = localQuery.getAfterId();
        if (StringUtils.isNotBlank(afterId)) {
            RecordRef afterRecordRef = new RecordRef(afterId);
            String source = afterRecordRef.getSourceId();
            while (sourceIdx < recordsDao.size() && !recordsDao.get(sourceIdx).getId().equals(source)) {
                sourceIdx++;
            }
            localQuery.setAfterId(afterRecordRef.getId());
        }

        while (sourceIdx < recordsDao.size() && result.getRecords().size() < query.getMaxItems()) {

            localQuery.setMaxItems(query.getMaxItems() - result.getRecords().size());
            RecordsDAO recordsDAO = recordsDao.get(sourceIdx);
            DaoRecordsResult daoRecords = recordsDAO.queryRecords(localQuery);

            List<String> recordsWithSource = new ArrayList<>();
            daoRecords.getRecords().forEach(r ->
                    recordsWithSource.add(new RecordRef(recordsDAO.getId(), r).toString()));
            daoRecords.setRecords(recordsWithSource);

            result.merge(daoRecords);

            if (++sourceIdx < recordsDao.size()) {

                result.setHasMore(true);

                if (localQuery.isAfterIdMode()) {
                    localQuery.setAfterId(null);
                } else {
                    long skip = localQuery.getSkipCount() - daoRecords.getTotalCount();
                    localQuery.setSkipCount((int) Math.max(skip, 0));
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, JsonNode> queryMeta(Collection<String> records, String gqlSchema) {
        return queryMeta(records, (source, sourceRecords) -> source.queryMeta(sourceRecords, gqlSchema));
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        return queryMeta(records, (source, sourceRecords) -> source.queryMeta(sourceRecords, metaClass));
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        RecordRef recordRef = new RecordRef(id);
        return getSource(recordRef.getSourceId()).getMetaValue(context, recordRef.getId());
    }

    @Override
    public GroupAction<String> createAction(String actionId, GroupActionConfig config) {

        GroupActionConfig recActConfig = new GroupActionConfig();
        recActConfig.setBatchSize(30);

        GroupAction<RecordRef> recordAction = new MultiSourceGroupAction(recActConfig,
                                                                         config,
                                                                         actionId,
                                                                         this::getSource);

        return new ConvertGroupAction<>(recordAction, Object::toString, RecordRef::new);
    }

    private <V> Map<String, V> queryMeta(Collection<String> records,
                                         BiFunction<RecordsDAO, Collection<String>, Map<String, V>> metaFunc) {
        Map<String, V> result = new HashMap<>();
        groupBySource(records).forEach((source, sourceRecords) -> {
            Map<String, V> sourceMeta = metaFunc.apply(getSource(source), sourceRecords);
            sourceMeta.forEach((record, meta) -> result.put(new RecordRef(source, record).toString(), meta));
        });
        return result;
    }

    private Map<String, Set<String>> groupBySource(Collection<String> records) {
        Map<String, Set<String>> result = new HashMap<>();
        for (String record : records) {
            RecordRef ref = new RecordRef(record);
            String sourceId = ref.getSourceId();
            String recordId = ref.getId();
            result.computeIfAbsent(sourceId, key -> new HashSet<>()).add(recordId);
        }
        return result;
    }

    private RecordsDAO getSource(String id) {
        return daoBySource.get(id);
    }

    public void setRecordsDao(List<RecordsDAO> records) {
        this.recordsDao = records;
        records.forEach(r -> daoBySource.put(r.getId(), r));
    }
}
