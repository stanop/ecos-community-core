package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GroupActionConfig {

    private ObjectNode params;
    private int batchSize = 1;
    private boolean async = false;
    private int maxResults = 100;
    private int maxErrors = 1000;
    private long timeout = TimeUnit.HOURS.toMillis(5);

    public GroupActionConfig() {
    }

    public GroupActionConfig(GroupActionConfig other) {
        this.params = other.getParams().deepCopy();
        this.batchSize = other.batchSize;
    }

    public ObjectNode getParams() {
        if (params == null) {
            params = JsonNodeFactory.instance.objectNode();
        }
        return params;
    }

    public void setParams(ObjectNode params) {
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

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupActionConfig that = (GroupActionConfig) o;

        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }

    @Override
    public String toString() {
        return "GroupActionConfig{" +
                "params=" + params +
                ", batchSize=" + batchSize +
                ", async=" + async +
                ", maxResults=" + maxResults +
                ", maxErrors=" + maxErrors +
                ", timeout=" + timeout +
                '}';
    }
}
