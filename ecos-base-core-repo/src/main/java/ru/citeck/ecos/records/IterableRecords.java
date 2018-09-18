package ru.citeck.ecos.records;

import ru.citeck.ecos.records.query.RecordsQuery;

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
        this.recordsQuery = recordsQuery;
        this.recordsService = recordsService;
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
        private RecordRef lastId = null;
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