package ru.citeck.ecos.journals.records;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.List;

public class RecordsResult {

    public final List<RemoteNodeRef> records;
    public final boolean hasNext;
    public final long totalCount;

    RecordsResult(List<RemoteNodeRef> records, boolean hasNext, long totalCount) {
        this.records = records;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
    }
}
