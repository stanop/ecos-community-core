package ru.citeck.ecos.records;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class IterableRecordsMeta<T> implements Iterable<RecordInfo<T>> {

    private static final int META_BATCH_SIZE = 20;

    private final Iterable<RecordRef> recordRefs;
    private final Class<T> iterableType;
    private final RecordsService recordsService;

    public IterableRecordsMeta(Iterable<RecordRef> recordRefs,
                               RecordsService recordsService,
                               Class<T> iterableType) {
        this.recordRefs = recordRefs;
        this.iterableType = iterableType;
        this.recordsService = recordsService;
    }

    @Override
    public Iterator<RecordInfo<T>> iterator() {
        return new MetaIterator(recordRefs.iterator());
    }

    class MetaIterator implements Iterator<RecordInfo<T>> {

        private Iterator<RecordRef> records;
        private List<RecordInfo<T>> recordsInfo;

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
                recordsInfo = new ArrayList<>();
                metaByRef.forEach((ref, data) -> recordsInfo.add(new RecordInfo<>(ref, data)));
            } else {
                recordsInfo = Collections.emptyList();
            }
        }

        @Override
        public boolean hasNext() {
            if (recordsInfo == null || currentIdx >= recordsInfo.size()) {
                takeNextMeta();
            }
            return currentIdx < recordsInfo.size();
        }

        @Override
        public RecordInfo<T> next() {
            return recordsInfo.get(currentIdx++);
        }
    }
}
