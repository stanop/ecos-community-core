package ru.citeck.ecos.records.request.mutation;

import ru.citeck.ecos.records.RecordMeta;

import java.util.ArrayList;
import java.util.List;

public class RecordsMutation {

    private List<RecordMeta> records = new ArrayList<>();
    private String sourceId;
    private boolean debug = false;

    public List<RecordMeta> getRecords() {
        return records;
    }

    public void setRecords(List<RecordMeta> records) {
        if (records != null) {
            this.records = new ArrayList<>(records);
        } else {
            this.records.clear();
        }
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId != null ? sourceId : "";
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
