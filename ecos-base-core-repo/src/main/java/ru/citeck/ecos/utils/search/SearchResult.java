package ru.citeck.ecos.utils.search;

import java.util.Collections;
import java.util.List;

public class SearchResult<T> {

    private List<T> items = Collections.emptyList();
    private boolean hasMore = false;
    private long totalCount = 0;

    public SearchResult() {
    }

    public SearchResult(List<T> items, boolean hasMore, long totalCount) {
        this.items = items;
        this.hasMore = hasMore;
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items != null ? items : Collections.emptyList();
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}

