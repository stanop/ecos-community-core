package ru.citeck.ecos.records.status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.CrudRecordsDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Makarskiy
 */
@Component
public class StatusRecords extends CrudRecordsDAO<StatusDTO> {

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
    public List<StatusDTO> getValuesToMutate(List<RecordRef> records) {
        throw new IllegalArgumentException("This operation not supported");
    }

    @Override
    public RecordsMutResult save(List<StatusDTO> values) {
        throw new IllegalArgumentException("This operation not supported");
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        throw new IllegalArgumentException("This operation not supported");
    }

    @Override
    public List<StatusDTO> getMetaValues(List<RecordRef> records) {
        List<StatusDTO> result = new ArrayList<>();

        for (RecordRef recordRef : records) {
            String id = recordRef.getId();
            if (StringUtils.isBlank(id)) {
                result.add(new StatusDTO());
                continue;
            }

            StatusDTO found = statusRecordsUtils.getByNameCaseOrDocumentStatus(id);
            result.add(found);
        }

        return result;
    }

    @Override
    public RecordsQueryResult<StatusDTO> getMetaValues(RecordsQuery recordsQuery) {
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