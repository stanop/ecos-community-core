package ru.citeck.ecos.journals.records;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;

public class RecordsResult {

    public final List<RemoteRef> records;
    public final boolean hasNext;
    public final long totalCount;

    RecordsResult(List<RemoteRef> records, boolean hasNext, long totalCount) {
        this.records = records;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
    }
}
