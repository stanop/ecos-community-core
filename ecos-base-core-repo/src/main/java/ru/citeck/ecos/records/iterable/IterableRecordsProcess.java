package ru.citeck.ecos.records.iterable;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class IterableRecordsProcess implements Iterable<ActionResult<RecordRef>> {

    private static final int BATCH_SIZE = 100;

    private final Iterable<RecordRef> recordRefs;
    private final RecordsService recordsService;
    private final GroupActionConfig processConfig;

    public IterableRecordsProcess(Iterable<RecordRef> recordRefs,
                                  RecordsService recordsService,
                                  GroupActionConfig processConfig) {
        this.recordRefs = recordRefs;
        this.recordsService = recordsService;
        this.processConfig = processConfig;
    }

    @Override
    public Iterator<ActionResult<RecordRef>> iterator() {
        return new ResultsIterator(recordRefs.iterator());
    }

    class ResultsIterator implements Iterator<ActionResult<RecordRef>> {

        private Iterator<RecordRef> records;
        private List<ActionResult<RecordRef>> results;

        private int currentIdx;

        ResultsIterator(Iterator<RecordRef> records) {
            this.records = records;
        }

        @Override
        public boolean hasNext() {

            if (results == null || currentIdx >= results.size()) {

                currentIdx = 0;

                List<RecordRef> recordRefs = new ArrayList<>();
                while (recordRefs.size() < BATCH_SIZE && records.hasNext()) {
                    recordRefs.add(records.next());
                }
                ActionResults<RecordRef> results = recordsService.executeAction(recordRefs, processConfig);
                this.results = results.getResults();
                if (results.getCancelCause() != null) {
                    throw new RuntimeException(results.getCancelCause());
                }
            }

            return results != null && currentIdx < results.size();
        }

        @Override
        public ActionResult<RecordRef> next() {
            return results.get(currentIdx++);
        }
    }
}
