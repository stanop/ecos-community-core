package ru.citeck.ecos.records.request.query;

import ru.citeck.ecos.records.RecordRef;

import java.util.ArrayList;
import java.util.List;

public class RecordsRefsResult extends RecordsResult<RecordRef> {

    public RecordsRefsResult() {
    }

    public RecordsRefsResult(RecordsRefsResult other) {
        super(other);
    }

    public RecordsRefsResult addSourceId(String sourceId) {
        List<RecordRef> records = new ArrayList<>();
        for (RecordRef ref : getRecords()) {
            records.add(new RecordRef(sourceId, ref.toString()));
        }
        setRecords(records);
        return this;
    }
}
