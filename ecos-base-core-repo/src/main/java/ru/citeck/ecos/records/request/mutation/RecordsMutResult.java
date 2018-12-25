package ru.citeck.ecos.records.request.mutation;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.RequestDebug;

import java.util.ArrayList;
import java.util.List;

public class RecordsMutResult extends RequestDebug {

    private List<RecordRef> records = new ArrayList<>();

    public List<RecordRef> getRecords() {
        return records;
    }

    public void add(RecordRef recordRef) {
        records.add(recordRef);
    }

    public void setRecords(List<RecordRef> records) {
        this.records = records != null ? records : new ArrayList<>();
    }
}
