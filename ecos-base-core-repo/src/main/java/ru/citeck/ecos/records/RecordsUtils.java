package ru.citeck.ecos.records;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecordsUtils {
/*

    public static Map<String, Set<String>> groupStrBySource(Collection<String> records) {
        return groupBySource(records, RecordRef::new, (r, d) -> r);
    }
*/

    public static Map<String, Set<RecordRef>> groupRefBySource(Collection<RecordRef> records) {
        return groupBySource(records, r -> r, (r, d) -> r);
    }

    public static <T> Map<String, Set<RecordInfo<T>>> groupInfoBySource(Collection<RecordInfo<T>> records) {
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

    private static <I, O> Map<String, Set<O>> groupBySource(Collection<I> records,
                                                            Function<I, RecordRef> getRecordRef,
                                                            BiFunction<RecordRef, I, O> toOutput) {
        Map<String, Set<O>> result = new HashMap<>();
        for (I recordData : records) {
            RecordRef record = getRecordRef.apply(recordData);
            String sourceId = record.getSourceId();
            Set<O> outSet = result.computeIfAbsent(sourceId, key -> new HashSet<>());
            outSet.add(toOutput.apply(record, recordData));
        }
        return result;
    }

    public static List<RecordRef> toRecords(Collection<String> strRecords) {
        return strRecords.stream().map(RecordRef::new).collect(Collectors.toList());
    }
}
