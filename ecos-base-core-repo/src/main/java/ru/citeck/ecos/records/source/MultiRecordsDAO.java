package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author Pavel Simonov
 */
public class MultiRecordsDAO extends AbstractRecordsDAO implements RecordsDAODelegate {

    private List<RecordsDAO> recordsDao;
    private Map<String, RecordsDAO> daoBySource = new ConcurrentHashMap<>();

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {

        DaoRecordsResult result = new DaoRecordsResult(query);

        RecordsQuery localQuery = new RecordsQuery(query);

        int sourceIdx = 0;
        RecordRef afterId = localQuery.getAfterId();
        if (afterId != null) {
            String source = afterId.getSourceId();
            while (sourceIdx < recordsDao.size() && !recordsDao.get(sourceIdx).getId().equals(source)) {
                sourceIdx++;
            }
        }

        while (sourceIdx < recordsDao.size() && result.getRecords().size() < query.getMaxItems()) {

            localQuery.setMaxItems(query.getMaxItems() - result.getRecords().size());
            RecordsDAO recordsDAO = recordsDao.get(sourceIdx);
            DaoRecordsResult daoRecords = recordsDAO.queryRecords(localQuery);

            List<String> recordsWithSource = new ArrayList<>();
            Function<String, String> recordRefMapping;
            if (recordsDAO instanceof RecordsDAODelegate) {
                recordRefMapping = r -> r;
            } else {
                recordRefMapping = r -> new RecordRef(recordsDAO.getId(), r).toString();
            }
            daoRecords.getRecords().forEach(r -> recordsWithSource.add(recordRefMapping.apply(r)));
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
        throw new RuntimeException("Is not supported. Use RecordsService instead");
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        throw new RuntimeException("Is not supported. Use RecordsService instead");
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        throw new RuntimeException("Is not supported. Use RecordsService instead");
    }

    public void setRecordsDao(List<RecordsDAO> records) {
        this.recordsDao = records;
        records.forEach(r -> daoBySource.put(r.getId(), r));
    }
}
