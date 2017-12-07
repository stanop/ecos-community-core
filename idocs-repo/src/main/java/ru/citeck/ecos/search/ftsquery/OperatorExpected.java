package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;

import java.util.List;

public interface OperatorExpected {

    /**
     * Operator OR
     */
    OperandExpected or();

    /**
     * Operator AND
     */
    OperandExpected and();

    /**
     * Close bracket
     */
    OperatorExpected close();

    /**
     * Search in database instead of SOLR
     */
    OperatorExpected transactional();

    /**
     * Search in SOLR without database
     */
    OperandExpected eventual();

    /**
     * Query one node
     */
    NodeRef queryOne(SearchService searchService);

    /**
     * Query nodes
     */
    List<NodeRef> query(SearchService searchService);

    /**
     * Get query string
     */
    String getQuery();
}