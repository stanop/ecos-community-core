package ru.citeck.ecos.records.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordsResult<T> {

    private List<T> records = Collections.emptyList();
    private boolean hasMore = false;
    private long totalCount = 0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObjectNode debug = null;

    public RecordsResult() {
    }

    public RecordsResult(RecordsResult<T> other) {
        records = new ArrayList<>(other.getRecords());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
        debug = other.debug;
    }

    public <K> RecordsResult(RecordsResult<K> other, Function<K, T> mapper) {
        records = other.getRecords().stream().map(mapper).collect(Collectors.toList());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
        debug = other.debug;
    }

    public void merge(RecordsResult<T> other) {

        List<T> records = new ArrayList<>();
        records.addAll(this.records);
        records.addAll(other.getRecords());
        this.records = records;

        hasMore = other.getHasMore();
        totalCount += other.getTotalCount();
    }

    public void setDebugInfo(Class<?> clazz, String key, String value) {
        setDebugInfo(clazz.getSimpleName(), key, value);
    }

    public void setDebugInfo(Class<?> clazz, String key, Object value) {
        setDebugInfo(clazz.getSimpleName(), key, String.valueOf(value));
    }

    public void setDebugInfo(String sourceKey, String key, String value) {
        ObjectNode debug = getNotNullDebug();
        JsonNode sourceNode = debug.get(sourceKey);
        ObjectNode target;
        if (sourceNode == null || !sourceNode.isObject()) {
            target = JsonNodeFactory.instance.objectNode();
            debug.put(sourceKey, target);
        } else {
            target = (ObjectNode) sourceNode;
        }
        target.put(key, TextNode.valueOf(value));
    }

    public void setDebugInfo(String key, String value) {
        setDebugInfo(key, TextNode.valueOf(value));
    }

    public void setDebugInfo(String key, JsonNode value) {
        getNotNullDebug().set(key, value);
    }

    private ObjectNode getNotNullDebug() {
        if (debug == null) {
            debug = JsonNodeFactory.instance.objectNode();
        }
        return debug;
    }

    public ObjectNode getDebug() {
        return debug;
    }

    public void setDebug(ObjectNode debug) {
        this.debug = debug;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records != null ? records : Collections.emptyList();
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
