package ru.citeck.ecos.records.request.mutation;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.RespRecord;
import ru.citeck.ecos.records.request.RequestResDebug;

import java.util.ArrayList;
import java.util.List;

public class RecordsMutResult extends RequestResDebug {

    private List<RespRecord> records = new ArrayList<>();

    public List<RespRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RespRecord> records) {
        if (records != null) {
            this.records = new ArrayList<>(records);
        } else {
            this.records = new ArrayList<>();
        }
    }

    public void add(RecordRef recordRef) {
        records.add(new RespRecord(recordRef));
    }

    public void add(RespRecord record) {
        records.add(record);
    }
}
