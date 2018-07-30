package ru.citeck.ecos.action.group;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroupActionConfig {

    private Map<String, String> params = Collections.emptyMap();
    private int batchSize = 1;

    public GroupActionConfig() {
    }

    public GroupActionConfig(GroupActionConfig other) {
        this.params = new HashMap<>();
        this.params.putAll(other.params);
        this.batchSize = other.batchSize;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
