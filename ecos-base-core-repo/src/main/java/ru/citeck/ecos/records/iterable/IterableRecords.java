package ru.citeck.ecos.records.iterable;

import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;

import java.util.*;

public class IterableRecords implements Iterable<RecordRef> {

    private static final int SEARCH_MAX_ITEMS = 100;

    private final String sourceId;
    private final RecordsQuery recordsQuery;
    private final RecordsService recordsService;

    public IterableRecords(RecordsService recordsService,
                           String sourceId,
                           RecordsQuery recordsQuery) {

        this.sourceId = sourceId;
        this.recordsQuery = new RecordsQuery(recordsQuery);
        this.recordsService = recordsService;

        ParameterCheck.mandatory("recordsService", recordsService);
        ParameterCheck.mandatory("recordsQuery", recordsQuery);
        ParameterCheck.mandatory("sourceId", sourceId);
    }

    public IterableRecords(RecordsQuery recordsQuery, RecordsService recordsService) {
        this(recordsService, AlfNodesRecordsDAO.ID, recordsQuery);
    }

    @Override
    public Iterator<RecordRef> iterator() {
        return new RecordsIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IterableRecords that = (IterableRecords) o;

        return Objects.equals(recordsQuery, that.recordsQuery);
    }

    @Override
    public int hashCode() {
        return recordsQuery.hashCode();
    }

    private class RecordsIterator implements Iterator<RecordRef> {

        private int currentIdx = 0;
        private List<RecordRef> records;
        private RecordRef lastId = recordsQuery.getAfterId();
        private boolean stopped = false;

        private int processedCount = 0;

        private void takeNextRecords() {

            currentIdx = 0;

            RecordsQuery query = new RecordsQuery(recordsQuery);
            query.setAfterId(lastId);
            query.setMaxItems(SEARCH_MAX_ITEMS);

            records = recordsService.getRecords(sourceId, query).getRecords();

            if (records.size() > 0) {
                RecordRef newLastId = records.get(records.size() - 1);
                if (!Objects.equals(newLastId, lastId)) {
                    lastId = newLastId;
                } else {
                    stopped = true;
                }
            }
        }

        @Override
        public boolean hasNext() {
            int maxItems = recordsQuery.getMaxItems();
            if (maxItems > 0 && processedCount >= maxItems) {
                return false;
            }
            if (records == null || currentIdx >= records.size() && currentIdx > 0) {
                takeNextRecords();
            }
            return !stopped && currentIdx < records.size();
        }

        @Override
        public RecordRef next() {
            int maxItems = recordsQuery.getMaxItems();
            if (stopped || (maxItems > 0 && processedCount >= maxItems)) {
                throw new NoSuchElementException();
            }
            processedCount++;
            return records.get(currentIdx++);
        }
    }
}