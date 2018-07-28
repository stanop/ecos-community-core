package ru.citeck.ecos.journals.records;

import com.fasterxml.jackson.databind.module.SimpleModule;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.repo.RemoteNodeRef;
import ru.citeck.ecos.search.*;

import java.util.Iterator;

/**
 * @author Pavel Simonov
 */
public class IterableJournalRecords implements Iterable<RemoteNodeRef> {

    private static final int SEARCH_MAX_ITEMS = 100;

    private JournalRecordsDAO recordsDAO;

    private final String query;
    private final String journalId;
    private final String language;

    public IterableJournalRecords(JournalRecordsDAO recordsDAO,
                                  String query,
                                  String journalId,
                                  String language) {

        this.recordsDAO = recordsDAO;
        this.query = query;
        this.journalId = journalId;
        this.language = language;

        SimpleModule module = new SimpleModule();
        module.addSerializer(new SearchCriteriaSerializer());
    }

    @Override
    public Iterator<RemoteNodeRef> iterator() {
        return new RecordsIterator();
    }

    private class RecordsIterator implements Iterator<RemoteNodeRef> {

        private int currentIdx = 0;
        private RecordsResult records;
        private String lastId = "";

        private void takeNextRecords() {
            currentIdx = 0;
            JournalGqlPageInfoInput pageInfo = new JournalGqlPageInfoInput(lastId, SEARCH_MAX_ITEMS, null);
            records = recordsDAO.getRecords(query, language, journalId, pageInfo);
            if (records.records.size() > 0) {
                lastId = records.records.get(records.records.size() - 1).toString();
            }
        }

        @Override
        public boolean hasNext() {
            if (records == null || currentIdx >= records.records.size() && currentIdx > 0) {
                takeNextRecords();
            }
            return currentIdx < records.records.size();
        }

        @Override
        public RemoteNodeRef next() {
            return records.records.get(currentIdx++);
        }
    }
}
