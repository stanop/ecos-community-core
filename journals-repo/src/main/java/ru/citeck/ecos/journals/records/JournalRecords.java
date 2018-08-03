package ru.citeck.ecos.journals.records;

import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author Pavel Simonov
 */
public class JournalRecords implements Iterable<RemoteRef> {

    private static final int SEARCH_MAX_ITEMS = 100;

    private JournalRecordsDAO recordsDAO;

    private final String query;
    private final String language;
    private final JournalType journalType;
    private final JGqlPageInfoInput pageInfo;

    public JournalRecords(JournalRecordsDAO recordsDAO,
                          JournalType journalType,
                          String query,
                          String language,
                          JGqlPageInfoInput pageInfo) {

        this.journalType = journalType;
        this.recordsDAO = recordsDAO;
        this.language = language;
        this.pageInfo = pageInfo;
        this.query = query;
    }

    @Override
    public Iterator<RemoteRef> iterator() {
        return new RecordsIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JournalRecords that = (JournalRecords) o;

        return Objects.equals(query, that.query) &&
               Objects.equals(language, that.language) &&
               Objects.equals(journalType, that.journalType) &&
               Objects.equals(pageInfo, that.pageInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, language, journalType, pageInfo);
    }

    private class RecordsIterator implements Iterator<RemoteRef> {

        private int currentIdx = 0;
        private RecordsResult records;
        private String lastId = "";
        private boolean stopped = false;

        private int processedCount = 0;

        private void takeNextRecords() {
            currentIdx = 0;
            JGqlPageInfoInput recordsPageInfo;
            recordsPageInfo = new JGqlPageInfoInput(lastId, SEARCH_MAX_ITEMS, pageInfo.getSortBy(), 0);
            records = recordsDAO.getRecords(journalType, query, language, recordsPageInfo);
            if (records.records.size() > 0) {
                String newLastId = records.records.get(records.records.size() - 1).toString();
                if (!Objects.equals(newLastId, lastId)) {
                    lastId = newLastId;
                } else {
                    stopped = true;
                }
            }
        }

        @Override
        public boolean hasNext() {
            int maxItems = pageInfo.getMaxItems();
            if (maxItems > 0 && processedCount >= maxItems) {
                return false;
            }
            if (records == null || currentIdx >= records.records.size() && currentIdx > 0) {
                takeNextRecords();
            }
            return !stopped && currentIdx < records.records.size();
        }

        @Override
        public RemoteRef next() {
            int maxItems = pageInfo.getMaxItems();
            if (stopped || (maxItems > 0 && processedCount >= maxItems)) {
                throw new NoSuchElementException();
            }
            processedCount++;
            return records.records.get(currentIdx++);
        }
    }
}
