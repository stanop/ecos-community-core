package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.records.request.result.RecordsResult;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecordsUtils {

    public static <T> List<ActionResult<RecordRef>> processList(List<RecordRef> records,
                                                                Function<RecordRef, T> toNode,
                                                                Function<List<T>, Map<T, ActionStatus>> process) {

        List<ActionResult<RecordRef>> results = new ArrayList<>();

        Map<T, RecordRef> mapping = new HashMap<>();

        List<T> nodes = new ArrayList<>();
        for (RecordRef recordRef : records) {
            T node = toNode.apply(recordRef);
            if (node == null) {
                ActionStatus status = new ActionStatus(ActionStatus.STATUS_SKIPPED);
                status.setMessage(recordRef + " is not a valid node");
                results.add(new ActionResult<>(recordRef, status));
                continue;
            }
            mapping.put(node, recordRef);
            nodes.add(node);
        }

        Map<T, ActionStatus> statuses = process.apply(nodes);
        statuses.forEach((node, status) ->
            results.add(new ActionResult<>(mapping.get(node), status))
        );

        return results;
    }

    public static RecordRef getRecordId(ObjectNode recordMeta) {
        JsonNode idNode = recordMeta.get("id");
        String id = idNode != null && idNode.isTextual() ? idNode.asText() : null;
        return id != null ? new RecordRef(id) : null;
    }

    public static NodeRef toNodeRef(RecordRef recordRef) {
        String nodeRefStr = recordRef.getId();
        int sourceDelimIdx = nodeRefStr.lastIndexOf(RecordRef.SOURCE_DELIMITER);
        if (sourceDelimIdx > -1) {
            nodeRefStr = nodeRefStr.substring(sourceDelimIdx + 1);
        }
        if (NodeRef.isNodeRef(nodeRefStr)) {
            return new NodeRef(nodeRefStr);
        }
        return null;
    }

    public static List<RecordRef> toLocalRecords(Collection<RecordRef> records) {
        return records.stream()
                      .map(r -> new RecordRef(r.getId()))
                      .collect(Collectors.toList());
    }

    public static List<NodeRef> toNodeRefs(List<RecordRef> records) {
        return records.stream()
                      .map(r -> {
                          String id = r.getId();
                          int lastDelim = id.lastIndexOf(RecordRef.SOURCE_DELIMITER);
                          if (lastDelim > -1) {
                              id = id.substring(lastDelim + 1);
                          }
                          return new NodeRef(id);
                      })
                      .collect(Collectors.toList());
    }

    public static List<ObjectNode> toScopedRecordsMeta(String sourceId, List<ObjectNode> records) {
        if (StringUtils.isBlank(sourceId)) {
            return records;
        }
        return records.stream().map(n -> {
            ObjectNode node;
            if (n.has("id")) {
                node = n.deepCopy();
                node.set("id", TextNode.valueOf(new RecordRef(sourceId, n.get("id").asText()).toString()));
            } else {
                node = n;
            }
            return node;
        }).collect(Collectors.toList());
    }

    public static RecordsResult<RecordRef> toScoped(String sourceId, RecordsResult<RecordRef> result) {
        return new RecordsResult<>(result, r -> new RecordRef(sourceId, r));
    }

    public static List<RecordRef> toScopedRecords(String sourceId, List<RecordRef> records) {
        return records.stream()
                      .map(r -> new RecordRef(sourceId, r))
                      .collect(Collectors.toList());
    }

    public static List<RecordRef> strToRecords(String sourceId, List<String> records) {
        return records.stream()
                      .map(r -> new RecordRef(sourceId, r))
                      .collect(Collectors.toList());
    }

    public static List<RecordRef> nodeRefsToRecords(String sourceId, List<NodeRef> records) {
        return records.stream()
                      .map(r -> new RecordRef(sourceId, r.toString()))
                      .collect(Collectors.toList());
    }

    public static Map<String, List<RecordRef>> groupRefBySource(Collection<RecordRef> records) {
        return groupBySource(records, r -> r, (r, d) -> r);
    }

    public static <T> Map<String, List<RecordInfo<T>>> groupInfoBySource(Collection<RecordInfo<T>> records) {
        return groupBySource(records, RecordInfo::getRef, (r, d) -> d);
    }

    public static <V> Map<RecordRef, V> convertToRefs(Map<String, V> data) {
        Map<RecordRef, V> result = new HashMap<>();
        data.forEach((id, recMeta) -> result.put(new RecordRef(id), recMeta));
        return result;
    }

    public static <V> Map<RecordRef, V> convertToRefs(String sourceId, Map<String, V> data) {
        Map<RecordRef, V> result = new HashMap<>();
        data.forEach((id, recMeta) -> result.put(new RecordRef(sourceId, id), recMeta));
        return result;
    }

    public static  List<ObjectNode> convertToRefs(String sourceId, List<ObjectNode> data) {
        return toScopedRecordsMeta(sourceId, data);
    }

    private static <I, O> Map<String, List<O>> groupBySource(Collection<I> records,
                                                            Function<I, RecordRef> getRecordRef,
                                                            BiFunction<RecordRef, I, O> toOutput) {
        Map<String, List<O>> result = new HashMap<>();
        for (I recordData : records) {
            RecordRef record = getRecordRef.apply(recordData);
            String sourceId = record.getSourceId();
            List<O> outList = result.computeIfAbsent(sourceId, key -> new ArrayList<>());
            outList.add(toOutput.apply(record, recordData));
        }
        return result;
    }

    public static List<RecordRef> toRecords(Collection<String> strRecords) {
        return strRecords.stream().map(RecordRef::new).collect(Collectors.toList());
    }
}
