package ru.citeck.ecos.records.query;

import ru.citeck.ecos.records.RecordRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordsResult {

    private RecordsQuery query;
    private List<RecordRef> records = Collections.emptyList();
    private boolean hasMore = false;
    private long totalCount = 0;

    public RecordsResult() {
    }

    public RecordsResult(RecordsQuery recordsQuery) {
        this.query = recordsQuery;
    }

    public RecordsResult(RecordsResult other) {
        query = other.query;
        records = new ArrayList<>(other.getRecords());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
    }

    public RecordsResult addSourceId(String sourceId) {
        List<RecordRef> records = new ArrayList<>();
        for (RecordRef ref : getRecords()) {
            records.add(new RecordRef(sourceId, ref.toString()));
        }
        setRecords(records);
        return this;
    }

    public void merge(RecordsResult other) {

        List<RecordRef> records = new ArrayList<>();
        records.addAll(this.records);
        records.addAll(other.getRecords());
        this.records = records;

        hasMore = other.hasMore();
        totalCount += other.getTotalCount();
    }

    public RecordsQuery getQuery() {
        return query;
    }

    public void setQuery(RecordsQuery query) {
        this.query = query;
    }

    public List<RecordRef> getRecords() {
        return records;
    }

    public void setRecords(List<RecordRef> records) {
        this.records = records;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
