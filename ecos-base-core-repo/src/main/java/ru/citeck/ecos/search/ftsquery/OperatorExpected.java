package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
     * Search in database or in SOLR if query not supported by database
     */
    OperatorExpected transactionalIfPossible();

    /**
     * Search in SOLR without database
     */
    OperatorExpected eventual();

    /**
     * Set query consistency
     */
    OperatorExpected consistency(QueryConsistency consistency);

    /**
     * Query one node
     */
    Optional<NodeRef> queryOne(SearchService searchService);

    /**
     * Query nodes, filter it and get first element
     */
    Optional<NodeRef> queryOne(SearchService searchService, Predicate<NodeRef> filter);

    /**
     * Query nodes
     */
    List<NodeRef> query(SearchService searchService);

    /**
     * Query nodes and filter results
     */
    List<NodeRef> query(SearchService searchService, Predicate<NodeRef> filter);

    /**
     * Query nodes and additional info about result
     */
    QueryResult queryDetails(SearchService searchService);

    /**
     * Get query string
     */
    String getQuery();

    /**
     * Get copy of query
     */
    OperatorExpected copy();

    /**
     * Set bulk fetch
     */
    OperatorExpected bulkFetch(boolean value);

    /**
     * Set max items
     */
    OperatorExpected maxItems(int value);

    /**
     * Set skip count
     */
    OperatorExpected skipCount(int value);

    /**
     * Add sorting
     */
    OperatorExpected addSort(QName field, boolean ascending);
    OperatorExpected addSort(String field, boolean ascending);
    OperatorExpected addSort(SearchParameters.SortDefinition sortDefinition);

    /**
     * Set permissions mode
     */
    OperatorExpected permissionsMode(PermissionEvaluationMode mode);

    /**
     * Search all nodes without limits
     */
    OperatorExpected unlimited();
}
