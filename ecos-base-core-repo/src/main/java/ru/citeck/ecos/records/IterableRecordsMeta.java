package ru.citeck.ecos.records;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
public class IterableRecordsMeta<T> implements Iterable<T> {

    private static final int META_BATCH_SIZE = 20;
    private static final int MAX_REFS_MAPPING_SIZE = 100;

    private final Iterable<RecordRef> recordRefs;
    private final Class<T> iterableType;
    private final RecordsService recordsService;
    private final Map<T, RecordRef> refsMapping = new ConcurrentHashMap<>();

    public IterableRecordsMeta(Iterable<RecordRef> recordRefs,
                               RecordsService recordsService,
                               Class<T> iterableType) {
        this.recordRefs = recordRefs;
        this.iterableType = iterableType;
        this.recordsService = recordsService;
    }

    @Override
    public Iterator<T> iterator() {
        return new MetaIterator(recordRefs.iterator());
    }

    public RecordRef getRecordRef(T data) {
        return refsMapping.get(data);
    }

    class MetaIterator implements Iterator<T> {

        private Iterator<RecordRef> records;
        private List<T> meta;

        private int currentIdx = 0;

        public MetaIterator(Iterator<RecordRef> records) {
            this.records = records;
        }

        private void takeNextMeta() {

            currentIdx = 0;

            List<RecordRef> recordRefs = new ArrayList<>();
            while (recordRefs.size() < META_BATCH_SIZE && records.hasNext()) {
                recordRefs.add(records.next());
            }
            if (recordRefs.size() > 0) {
                Map<RecordRef, T> metaByRef = recordsService.getMeta(recordRefs, iterableType);
                if (metaByRef.size() < MAX_REFS_MAPPING_SIZE) {
                    metaByRef.forEach((k, v) -> refsMapping.put(v, k));
                }
                this.meta = new ArrayList<>(metaByRef.values());
            } else {
                meta = Collections.emptyList();
            }
        }

        @Override
        public boolean hasNext() {
            if (meta == null || currentIdx >= meta.size()) {
                takeNextMeta();
            }
            return currentIdx < meta.size();
        }

        @Override
        public T next() {
            return meta.get(currentIdx++);
        }
    }
}
