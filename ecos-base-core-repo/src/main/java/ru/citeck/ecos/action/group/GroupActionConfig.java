package ru.citeck.ecos.action.group;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GroupActionConfig {

    private Map<String, String> params = Collections.emptyMap();
    private int batchSize = 1;
    private boolean async = false;
    private int maxResults = 100;
    private int maxErrors = 1000;

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

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getMaxErrors() {
        return maxErrors;
    }

    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupActionConfig that = (GroupActionConfig) o;

        return batchSize == that.batchSize &&
               async == that.async &&
               maxResults == that.maxResults &&
               maxErrors == that.maxErrors &&
               Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(params);
        result = 31 * result + batchSize;
        result = 31 * result + (async ? 1 : 0);
        result = 31 * result + maxResults;
        result = 31 * result + maxErrors;
        return result;
    }

    @Override
    public String toString() {
        return "GroupActionConfig{" +
                "params=" + params +
                ", batchSize=" + batchSize +
                ", async=" + async +
                ", maxResults=" + maxResults +
                ", maxErrors=" + maxErrors +
                '}';
    }
}
