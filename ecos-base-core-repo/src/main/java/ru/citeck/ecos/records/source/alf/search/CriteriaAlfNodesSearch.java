package ru.citeck.ecos.records.source.alf.search;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.query.SortBy;
import ru.citeck.ecos.records2.request.query.lang.DistinctQuery;
import ru.citeck.ecos.search.*;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CriteriaAlfNodesSearch implements AlfNodesSearch {

    public static final String LANGUAGE = "criteria";

    private SearchService searchService;
    private FTSQueryBuilder ftsQueryBuilder;
    private NamespaceService namespaceService;
    private SearchCriteriaParser criteriaParser;
    private CriteriaSearchService criteriaSearchService;
    private NodeService nodeService;

    @Autowired
    public CriteriaAlfNodesSearch(CriteriaSearchService criteriaSearchService,
                                  SearchCriteriaParser criteriaParser,
                                  ServiceRegistry serviceRegistry,
                                  FTSQueryBuilder ftsQueryBuilder,
                                  AlfNodesRecordsDAO recordsSource) {

        this.criteriaSearchService = criteriaSearchService;
        this.criteriaParser = criteriaParser;
        this.ftsQueryBuilder = ftsQueryBuilder;
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();

        recordsSource.register(this);
    }

    @Override
    public RecordsQueryResult<RecordRef> queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated) {

        SearchCriteria criteria = criteriaParser.parse(query.getQuery());
        if (criteria.getTriplets().size() == 0) {
            return new RecordsQueryResult<>();
        }

        criteria.setSkip(query.getSkipCount());
        criteria.setLimit(query.getMaxItems());

        boolean afterIdMode = false;
        String afterIdSortField = "";

        if (afterDbId != null) {

            afterIdSortField = ContentModel.PROP_NODE_DBID.toPrefixString(namespaceService);
            criteria.addSort(afterIdSortField, SortOrder.ASCENDING);

            String predicate = SearchPredicate.NUMBER_GREATER_THAN.getValue();
            criteria.addCriteriaTriplet(afterIdSortField, predicate, String.valueOf(afterDbId));

            afterIdMode = true;
        }

        for (SortBy sortBy : query.getSortBy()) {
            if (!afterIdMode || !sortBy.getAttribute().equals(afterIdSortField)) {
                SortOrder order = sortBy.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;
                criteria.addSort(sortBy.getAttribute(), order);
            }
        }

        CriteriaSearchResults criteriaResults = criteriaSearchService.query(criteria, SearchService.LANGUAGE_FTS_ALFRESCO);

        RecordsQueryResult<RecordRef> result = new RecordsQueryResult<>();

        if (query.isDebug()) {
            result.setDebugInfo(getClass(), "query", criteriaResults.getQuery());
        }

        result.setRecords(criteriaResults.getResults()
                                         .stream()
                                         .map(r -> RecordRef.valueOf(r.toString()))
                                         .collect(Collectors.toList()));
        result.setTotalCount(criteriaResults.getTotalCount());
        result.setHasMore(criteriaResults.hasMore());

        return result;
    }

    @Override
    public List<Object> queryDistinctValues(DistinctQuery query, int max) {

        SearchCriteria criteria = criteriaParser.parse(query.getQuery());
        String ftsQuery = "(" + ftsQueryBuilder.buildQuery(criteria) + ")";

        QName distinctProp = QName.resolveToQName(namespaceService, query.getAttribute());

        Set<Object> values = new HashSet<>();
        Set<Object> newValues = new HashSet<>();

        int found, requests = 0;
        do {

            SearchParameters parameters = new SearchParameters();
            parameters.setMaxItems(max);
            parameters.setLimit(max);
            parameters.setQuery(ftsQuery);
            parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            parameters.setQueryConsistency(QueryConsistency.EVENTUAL);

            ResultSet resultSet = null;

            try {

                resultSet = searchService.query(parameters);
                found = resultSet.length();

                newValues.clear();

                for (NodeRef nodeRef : resultSet.getNodeRefs()) {

                    Object property = nodeService.getProperty(nodeRef, distinctProp);
                    if (property != null && !values.contains(property)) {
                        newValues.add(property);
                    }
                }

                for (Object value : newValues) {
                    ftsQuery +=  " AND NOT @" + distinctProp + ":\"" + value + "\"";
                }

                values.addAll(newValues);

            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }

        } while (found > 0 && values.size() <= max && ++requests <= max);

        return new ArrayList<>(values);
    }

    @Override
    public AfterIdType getAfterIdType() {
        return AfterIdType.DB_ID;
    }

    @Override
    public String getLanguage() {
        return LANGUAGE;
    }
}
