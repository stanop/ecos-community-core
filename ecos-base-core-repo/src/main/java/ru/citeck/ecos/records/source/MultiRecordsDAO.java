package ru.citeck.ecos.records.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
public class MultiRecordsDAO extends AbstractRecordsDAO
                             implements RecordsQueryDAO,
                                        RecordsActionExecutor {

    private static final Log logger = LogFactory.getLog(MultiRecordsDAO.class);

    private List<RecordsQueryDAO> recordsDao;
    private Map<String, RecordsQueryDAO> daoBySource = new ConcurrentHashMap<>();

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {

        RecordsQueryResult<RecordRef> result = new RecordsQueryResult<>();

        RecordsQuery localQuery = new RecordsQuery(query);

        int sourceIdx = 0;
        RecordRef afterId = localQuery.getAfterId();
        if (afterId != RecordRef.EMPTY) {
            String source = afterId.getSourceId();
            while (sourceIdx < recordsDao.size() && !recordsDao.get(sourceIdx).getId().equals(source)) {
                sourceIdx++;
            }
        }

        while (sourceIdx < recordsDao.size() && result.getRecords().size() < query.getMaxItems()) {

            localQuery.setMaxItems(query.getMaxItems() - result.getRecords().size());
            RecordsQueryDAO recordsDAO = recordsDao.get(sourceIdx);
            RecordsQueryResult<RecordRef> daoRecords = recordsDAO.getRecords(localQuery);

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

        if (result.getTotalCount() == query.getMaxItems() && result.getHasMore()) {
            result.setTotalCount(result.getTotalCount() + 1);
        }

        return result;
    }

    @Override
    public ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config) {
        ActionResults<RecordRef> results = new ActionResults<>();
        RecordsUtils.groupRefBySource(records).forEach((sourceId, sourceRecs) -> {
            RecordsQueryDAO recordsDAO = daoBySource.get(sourceId);
            if (recordsDAO instanceof RecordsActionExecutor) {
                results.merge(((RecordsActionExecutor) recordsDAO).executeAction(sourceRecs, config));
            } else {
                ActionStatus status = new ActionStatus(ActionStatus.STATUS_SKIPPED);
                status.setMessage("Source id " + sourceId + " doesn't support actions");
                for (RecordRef recordRef : sourceRecs) {
                    results.getResults().add(new ActionResult<>(recordRef, status));
                }
            }
        });
        return results;
    }

    public void setRecordsDao(List<RecordsQueryDAO> records) {
        this.recordsDao = records;
        records.forEach(r -> daoBySource.put(r.getId(), r));
    }
}
