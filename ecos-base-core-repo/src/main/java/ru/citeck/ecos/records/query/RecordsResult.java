package ru.citeck.ecos.records.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordsResult<T> {

    private List<T> records = Collections.emptyList();
    private boolean hasMore = false;
    private long totalCount = 0;

    public RecordsResult() {
    }

    public RecordsResult(RecordsResult<T> other) {
        records = new ArrayList<>(other.getRecords());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
    }

    public <K> RecordsResult(RecordsResult<K> other, Function<K, T> mapper) {
        records = other.getRecords().stream().map(mapper).collect(Collectors.toList());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
    }

    public void merge(RecordsResult<T> other) {

        List<T> records = new ArrayList<>();
        records.addAll(this.records);
        records.addAll(other.getRecords());
        this.records = records;

        hasMore = other.getHasMore();
        totalCount += other.getTotalCount();
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public boolean getHasMore() {
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
