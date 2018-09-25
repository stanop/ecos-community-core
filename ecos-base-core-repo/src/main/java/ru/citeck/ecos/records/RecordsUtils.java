package ru.citeck.ecos.records;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecordsUtils {

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
