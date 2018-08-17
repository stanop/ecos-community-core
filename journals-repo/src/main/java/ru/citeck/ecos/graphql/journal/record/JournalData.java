package ru.citeck.ecos.graphql.journal.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalData {

    private Data data;
    private Map<String, Object> errors;
    private Map<String, Object> extensions;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private JournalRecords journalRecords;

        public JournalRecords getJournalRecords() {
            return journalRecords;
        }

        public void setJournalRecords(JournalRecords journalRecords) {
            this.journalRecords = journalRecords;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JournalRecords {

        private Map<String, Object> records;
        private long totalCount;
        private PageInfo pageInfo;

        public Map<String, Object> getRecords() {
            return records;
        }

        public void setRecords(Map<String, Object> records) {
            this.records = records;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public PageInfo getPageInfo() {
            return pageInfo;
        }

        public void setPageInfo(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {

        private boolean hasNextPage;
        private int skipCount;
        private int maxItems;

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public int getSkipCount() {
            return skipCount;
        }

        public void setSkipCount(int skipCount) {
            this.skipCount = skipCount;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public void setMaxItems(int maxItems) {
            this.maxItems = maxItems;
        }

    }

}
