package ru.citeck.ecos.records.status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsQueryWithMetaLocalDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Makarskiy
 */
@Component
public class StatusRecords  extends LocalRecordsDAO implements RecordsMetaLocalDAO<StatusRecord>,
        RecordsQueryWithMetaLocalDAO<StatusRecord> {

    private static final String ID = "status";

    private final StatusRecordsUtils statusRecordsUtils;

    {
        setId(ID);
    }

    @Autowired
    public StatusRecords(StatusRecordsUtils statusRecordsUtils) {
        this.statusRecordsUtils = statusRecordsUtils;
    }

    @Override
    public List<StatusRecord> getMetaValues(List<RecordRef> records) {
        List<StatusRecord> result = new ArrayList<>();

        for (RecordRef recordRef : records) {
            String id = recordRef.getId();
            if (StringUtils.isBlank(id)) {
                result.add(new StatusRecord(new StatusDTO()));
                continue;
            }

            StatusDTO found = statusRecordsUtils.getByNameCaseOrDocumentStatus(id);
            result.add(new StatusRecord(found));
        }

        return result;
    }

    @Override
    public RecordsQueryResult<StatusRecord> getMetaValues(RecordsQuery recordsQuery) {
        StatusQuery query = recordsQuery.getQuery(StatusQuery.class);

        if (StringUtils.isNotBlank(query.getAllExisting())) {
            return statusRecordsUtils.getAllExistingStatuses(query.getAllExisting());
        }

        if (query.getAllAvailableToChange() != null) {
            return statusRecordsUtils.getAllAvailableToChangeStatuses(query.getAllAvailableToChange());
        }

        return statusRecordsUtils.getStatusByRecord(query.getRecord());
    }

}