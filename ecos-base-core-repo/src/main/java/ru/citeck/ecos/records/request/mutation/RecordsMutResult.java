package ru.citeck.ecos.records.request.mutation;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.RecordAttributes;
import ru.citeck.ecos.records.request.RequestResDebug;

import java.util.ArrayList;
import java.util.List;

public class RecordsMutResult extends RequestResDebug {

    private List<RecordAttributes> records = new ArrayList<>();

    public List<RecordAttributes> getRecords() {
        return records;
    }

    public void setRecords(List<RecordAttributes> records) {
        if (records != null) {
            this.records = new ArrayList<>(records);
        } else {
            this.records = new ArrayList<>();
        }
    }

    public void add(RecordRef recordRef) {
        records.add(new RecordAttributes(recordRef));
    }

    public void add(RecordAttributes record) {
        records.add(record);
    }
}
