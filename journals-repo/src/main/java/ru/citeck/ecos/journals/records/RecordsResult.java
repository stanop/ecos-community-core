package ru.citeck.ecos.journals.records;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;

public class RecordsResult {

    public final List<RemoteRef> records;
    public final long totalCount;
    public final boolean hasNext;
    public final int skipCount;
    public final int maxItems;

    public RecordsResult(List<RemoteRef> records, boolean hasNext, long totalCount, int skipCount, int maxItems) {
        this.records = records;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
        this.skipCount = skipCount;
        this.maxItems = maxItems;
    }
}
