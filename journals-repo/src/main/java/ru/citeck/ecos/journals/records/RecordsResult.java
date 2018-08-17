package ru.citeck.ecos.journals.records;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;

public class RecordsResult {

    public final List<RemoteRef> records;
    public final long totalCount;
    public final boolean hasNext;
    public final int skipCount;
    public final int maxItems;

    @JsonCreator
    public RecordsResult(@JsonProperty("records") List<RemoteRef> records,
                         @JsonProperty("hasNext") boolean hasNext,
                         @JsonProperty("totalCount") long totalCount,
                         @JsonProperty("skipCount") int skipCount,
                         @JsonProperty("maxItems") int maxItems) {
        this.records = records;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
        this.skipCount = skipCount;
        this.maxItems = maxItems;
    }
}
