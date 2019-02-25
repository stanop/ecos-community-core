package ru.citeck.ecos.records.request.mutation;

import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.request.result.RecordsResult;

public class RecordsMutResult extends RecordsResult<RecordMeta> {

    public RecordsMutResult() {
    }

    public RecordsMutResult(RecordsResult<RecordMeta> other) {
        super(other);
    }
}
