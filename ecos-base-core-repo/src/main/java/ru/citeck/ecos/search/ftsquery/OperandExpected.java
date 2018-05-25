package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

public interface OperandExpected {

    /**
     * Field has any of specified values.
     * Construct multiple key-value pairs and join it by OR operator
     * @param field field to check value
     * @param values values to check
     * @throws IllegalArgumentException if values is empty
     */
    OperatorExpected any(QName field, Iterable<Serializable> values);

    /**
     * Add multiple @key:"value" pairs to query
     * @param values values which would be added to query
     * @throws IllegalArgumentException if values is empty
     */
    OperatorExpected values(Map<QName, Serializable> values);

    /**
     * Add multiple @key:"value" pairs to query
     * @param values values which would be added to query
     * @param joinOperator operator to join specified pairs
     * @throws IllegalArgumentException if values is empty
     * @see BinOperator
     */
    OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator);

    /**
     * Add multiple @key:"value" pairs to query
     * @param values values which would be added to query
     * @param joinOperator operator to join specified pairs
     * @param exact search by exact match or not
     * @throws IllegalArgumentException if values is empty
     * @see BinOperator
     */
    OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator, boolean exact);

    /**
     * Field has exact value
     * If value is null then it will be empty() check
     * @param value value to check
     * @see FTSQuery#value(QName, Serializable)
     */
    OperatorExpected exact(QName field, Serializable value);

    /**
     * Field has value
     * If value is null then it will be empty() check
     * @param value value to check
     */
    OperatorExpected value(QName field, Serializable value);

    /**
     * Field has value.
     * If value is null then it will be empty() check
     * @param value value to check
     * @param exact search by exact match or not
     */
    OperatorExpected value(QName field, Serializable value, boolean exact);

    /**
     * Add path term
     */
    OperatorExpected path(String path);

    /**
     * Field is set
     */
    OperatorExpected isSet(QName field);

    /**
     * Field is unset
     */
    OperatorExpected isUnset(QName field);

    /**
     * Field is null
     */
    OperatorExpected isNull(QName field);

    /**
     * Field is not null
     */
    OperatorExpected isNotNull(QName field);

    /**
     * Field is null or unset
     * @param field field to check
     */
    OperatorExpected empty(QName field);

    /**
     * Field value is null on unset or empty string
     */
    OperatorExpected emptyString(QName field);

    /**
     * Search within parent
     */
    OperatorExpected parent(NodeRef parent);

    /**
     * Search by type
     */
    OperatorExpected type(QName typeName);

    /**
     * Operator NOT
     */
    OperandExpected not();

    /**
     * Open bracket
     */
    OperandExpected open();

    /**
     * Search in database instead of SOLR
     */
    OperandExpected transactional();

    /**
     * Search in database or in SOLR if query not supported by database
     */
    OperandExpected transactionalIfPossible();

    /**
     * Search in SOLR without database
     */
    OperandExpected eventual();

    /**
     * Set query consistency
     */
    OperandExpected consistency(QueryConsistency consistency);

    /**
     * Get copy of query
     */
    OperandExpected copy();

    /**
     * Set bulk fetch
     */
    OperandExpected bulkFetch(boolean value);

    /**
     * Set max items
     */
    OperandExpected maxItems(int value);

    /**
     * Set skip count
     */
    OperandExpected skipCount(int value);

    /**
     * Add sorting
     */
    OperandExpected addSort(QName field, boolean ascending);
    OperandExpected addSort(String field, boolean ascending);
    OperandExpected addSort(SearchParameters.SortDefinition sortDefinition);

    /**
     * Set permissions mode
     */
    OperandExpected permissionsMode(PermissionEvaluationMode mode);

    /**
     * Search all nodes without limits
     */
    OperandExpected unlimited();
}
