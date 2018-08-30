package ru.citeck.ecos.records.query;

import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RecordsQuery {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private int skipCount;
    private int maxItems;
    private List<SortBy> sortBy;
    private String afterId;
    private boolean afterIdMode = false;
    private QueryConsistency consistency;
    private String language = SearchService.LANGUAGE_FTS_ALFRESCO;
    private String query;

    public RecordsQuery() {
    }

    public RecordsQuery(RecordsQuery other) {
        this.skipCount = other.skipCount;
        this.maxItems = other.maxItems;
        this.afterId = other.afterId;
        this.afterIdMode = other.afterIdMode;
        this.consistency = other.consistency;
        this.language = other.language;
        this.query = other.query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public List<SortBy> getSortBy() {
        return sortBy;
    }

    public String getAfterId() {
        return afterId;
    }

    public void setAfterId(String afterId) {
        this.afterId = afterId;
        afterIdMode = true;
    }

    public void setAfterIdMode(boolean afterIdMode) {
        this.afterIdMode = afterIdMode;
    }

    public boolean isAfterIdMode() {
        return afterIdMode;
    }

    public void setSkipCount(Integer skipCount) {
        this.skipCount = skipCount != null ? skipCount : 0;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems != null ? maxItems : DEFAULT_PAGE_SIZE;
    }

    public void setSortBy(List<SortBy> sortBy) {
        this.sortBy = sortBy != null ? sortBy : Collections.emptyList();
    }

    public QueryConsistency getConsistency() {
        return consistency;
    }

    public void setConsistency(QueryConsistency consistency) {
        this.consistency = consistency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecordsQuery that = (RecordsQuery) o;

        return Objects.equals(skipCount, that.skipCount) &&
               Objects.equals(maxItems, that.maxItems) &&
               Objects.equals(sortBy, that.sortBy) &&
               Objects.equals(afterId, that.afterId) &&
               Objects.equals(consistency, that.consistency);
    }

    @Override
    public int hashCode() {
        int result = skipCount;
        result = 31 * result + maxItems;
        result = 31 * result + Objects.hashCode(sortBy);
        result = 31 * result + Objects.hashCode(afterId);
        result = 31 * result + Objects.hashCode(consistency);
        return result;
    }

    @Override
    public String toString() {
        return "RecordsQuery{" +
                "skipCount=" + skipCount +
                ", maxItems=" + maxItems +
                ", sortBy=" + sortBy +
                ", afterId='" + afterId + '\'' +
                ", consistency=" + consistency +
                '}';
    }
}
