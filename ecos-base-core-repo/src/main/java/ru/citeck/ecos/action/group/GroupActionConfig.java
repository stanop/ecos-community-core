package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

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

    public void setParam(String key, String value) {
        getParams().set(key, TextNode.valueOf(value));
    }

    public void setParam(String key, Boolean value) {
        getParams().set(key, BooleanNode.valueOf(value));
    }

    public void setPojoParam(String key, Object pojo) {
        getParams().set(key, JsonNodeFactory.instance.pojoNode(pojo));
    }

    @JsonIgnore
    public String getStrParam(String key) {
        JsonNode jsonNode = getParams().get(key);
        if (jsonNode == null || jsonNode instanceof NullNode) {
            return null;
        }
        return jsonNode.asText();
    }

    @JsonIgnore
    public <T> T getPojoParam(String key, Class<T> clazz, ObjectMapper mapper) {
        JsonNode jsonNode = getParams().get(key);
        if (jsonNode == null || jsonNode instanceof NullNode) {
            return null;
        }
        if (jsonNode instanceof POJONode) {
            Object pojo = ((POJONode) jsonNode).getPojo();
            if (pojo != null && clazz.isAssignableFrom(pojo.getClass())) {
                return (T) pojo;
            }
        }
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getBoolParam(String key) {
        return getBoolParam(key, false);
    }

    public boolean getBoolParam(String key, boolean def) {
        JsonNode jsonNode = getParams().get(key);
        if (jsonNode == null || jsonNode instanceof NullNode) {
            return def;
        }
        return jsonNode.asBoolean(def);
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
