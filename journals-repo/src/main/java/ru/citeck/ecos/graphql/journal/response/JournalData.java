package ru.citeck.ecos.graphql.journal.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalData {

    private Data data;
    private Object errors;
    private Map<Object, Object> extensions;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    public Map<Object, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<Object, Object> extensions) {
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

        private List<?> records;
        private long totalCount;
        private PageInfo pageInfo;

        public List<?> getRecords() {
            return records;
        }

        public void setRecords(List<?> records) {
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
