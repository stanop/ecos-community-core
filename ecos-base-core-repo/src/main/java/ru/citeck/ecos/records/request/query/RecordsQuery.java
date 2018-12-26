package ru.citeck.ecos.records.request.query;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.records.RecordRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RecordsQuery {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private String sourceId = "";
    private int skipCount;
    private int maxItems = -1;
    private List<SortBy> sortBy = Collections.emptyList();
    private RecordRef afterId;
    private boolean afterIdMode = false;
    private QueryConsistency consistency = QueryConsistency.DEFAULT;
    private String language = SearchService.LANGUAGE_FTS_ALFRESCO;
    private JsonNode query = MissingNode.getInstance();
    private boolean debug = false;

    public RecordsQuery() {
    }

    public RecordsQuery(RecordsQuery other) {
        this.query = other.query;
        this.afterId = other.afterId;
        this.maxItems = other.maxItems;
        this.sourceId = other.sourceId;
        this.language = other.language;
        this.skipCount = other.skipCount;
        this.afterIdMode = other.afterIdMode;
        this.consistency = other.consistency;
        this.sortBy = new ArrayList<>(other.sortBy);
        this.debug = other.debug;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public JsonNode getQuery() {
        return query;
    }

    @JsonSetter
    public void setQuery(JsonNode query) {
        if (query != null) {
            this.query = query;
        } else {
            this.query = MissingNode.getInstance();
        }
    }

    public void setQuery(String query) {
        if (StringUtils.isNotBlank(query)) {
            this.query = TextNode.valueOf(query);
        } else {
            this.query = MissingNode.getInstance();
        }
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

    public RecordRef getAfterId() {
        return afterId;
    }

    public void setAfterId(RecordRef afterId) {
        this.afterId = afterId;
        afterIdMode = true;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
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

        return Objects.equals(sourceId, that.sourceId) &&
               Objects.equals(skipCount, that.skipCount) &&
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
        result = 31 * result + Objects.hashCode(sourceId);
        result = 31 * result + Objects.hashCode(consistency);
        return result;
    }

    @Override
    public String toString() {
        return "RecordsQuery{" +
                "\"sourceId\":\"" + sourceId + "\"," +
                "\"skipCount\":\"" + skipCount + "\"," +
                "\"maxItems\":\"" + maxItems + "\"," +
                "\"sortBy\":\"" + sortBy + "\"," +
                "\"afterId\":\"" + afterId + "\"," +
                "\"afterIdMode\":\"" + afterIdMode + "\"," +
                "\"consistency\":\"" + consistency + "\"," +
                "\"language\":\"" + language + '\'' + "\"," +
                "\"query\":\"" + query + "\"," +
                '}';
    }
}
