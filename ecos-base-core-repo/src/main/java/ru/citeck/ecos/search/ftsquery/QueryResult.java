package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class QueryResult {

    private final long totalCount;
    private final boolean hasMore;
    private final List<NodeRef> nodeRefs;

    QueryResult(List<NodeRef> nodeRefs, boolean hasMore, long totalCount) {
        this.nodeRefs = nodeRefs;
        this.hasMore = hasMore;
        this.totalCount = totalCount;
    }

    public List<NodeRef> getNodeRefs() {
        return nodeRefs;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
