package ru.citeck.ecos.records.request.mutation;

import java.util.ArrayList;
import java.util.List;

public class RecordsMutation {

    private List<RecordMut> records = new ArrayList<>();
    private String sourceId = "";
    private boolean debug = false;

    public List<RecordMut> getRecords() {
        return records;
    }

    public void setRecords(List<RecordMut> records) {
        if (records != null) {
            this.records = records;
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
