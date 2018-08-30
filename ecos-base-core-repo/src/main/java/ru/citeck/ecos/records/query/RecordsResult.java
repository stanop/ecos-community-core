package ru.citeck.ecos.records.query;

import ru.citeck.ecos.records.RecordRef;

import java.util.function.Function;

public class RecordsResult extends BaseRecordsResult<RecordRef> {

    public RecordsResult() {
    }

    public RecordsResult(BaseRecordsResult<String> other, Function<String, RecordRef> converter) {
        super(other, converter);
    }
}
