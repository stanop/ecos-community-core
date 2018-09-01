package ru.citeck.ecos.records;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecordsUtils {

    public static Map<String, Set<String>> groupStrBySource(Collection<String> records) {
        return groupRefBySource(records.stream()
                                       .map(RecordRef::new)
                                       .collect(Collectors.toList()));
    }

    public static Map<String, Set<String>> groupRefBySource(Collection<RecordRef> records) {
        Map<String, Set<String>> result = new HashMap<>();
        for (RecordRef record : records) {
            String sourceId = record.getSourceId();
            String recordId = record.getId();
            result.computeIfAbsent(sourceId, key -> new HashSet<>()).add(recordId);
        }
        return result;
    }

    public static Collection<RecordRef> toRecords(Collection<String> strRecords) {
        return strRecords.stream().map(RecordRef::new).collect(Collectors.toList());
    }
}
