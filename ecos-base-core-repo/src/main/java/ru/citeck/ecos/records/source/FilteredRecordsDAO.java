package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class FilteredRecordsDAO extends AbstractRecordsDAO {

    private RecordsDAO targetDAO;

    @Override
    public RecordsResult<RecordRef> getRecords(RecordsQuery query) {

        RecordsQuery localQuery = new RecordsQuery(query);
        int maxItems = localQuery.getMaxItems();
        localQuery.setMaxItems((int) (1.5f * maxItems));

        RecordsResult<RecordRef> records = targetDAO.getRecords(localQuery);
        Function<List<RecordRef>, List<RecordRef>> filter = getFilter(query);

        List<RecordRef> filtered = filter.apply(records.getRecords());
        List<RecordRef> resultRecords = new ArrayList<>();

        int itemsCount = Math.min(filtered.size(), maxItems);
        for (int i = 0; i < itemsCount; i++) {
            resultRecords.add(filtered.get(i));
        }

        int totalDiff = records.getRecords().size() - filtered.size();
        records.setTotalCount(records.getTotalCount() - totalDiff);
        records.setRecords(resultRecords);

        return records;
    }

    protected abstract Function<List<RecordRef>, List<RecordRef>> getFilter(RecordsQuery query);

    public void setTargetDAO(RecordsDAO targetDAO) {
        this.targetDAO = targetDAO;
    }
}
