package ru.citeck.ecos.records.request.delete;

import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.request.result.RecordsResult;

public class RecordsDelResult extends RecordsResult<RecordMeta> {

    public RecordsDelResult() {
    }

    public RecordsDelResult(RecordsResult<RecordMeta> other) {
        super(other);
    }
}
