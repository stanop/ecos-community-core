package ru.citeck.ecos.records.source.alf.search;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.query.SortBy;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.search.*;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class CriteriaAlfNodesSearch implements AlfNodesSearch {

    public static final String LANGUAGE = "criteria";

    private CriteriaSearchService searchService;
    private SearchCriteriaParser criteriaParser;
    private NamespaceService namespaceService;

    @Autowired
    public CriteriaAlfNodesSearch(CriteriaSearchService searchService,
                                  SearchCriteriaParser criteriaParser,
                                  ServiceRegistry serviceRegistry,
                                  AlfNodesRecordsDAO recordsSource) {

        this.searchService = searchService;
        this.criteriaParser = criteriaParser;
        this.namespaceService = serviceRegistry.getNamespaceService();

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

        CriteriaSearchResults criteriaResults = searchService.query(criteria, SearchService.LANGUAGE_FTS_ALFRESCO);

        RecordsQueryResult<RecordRef> result = new RecordsQueryResult<>();

        if (query.isDebug()) {
            result.setDebugInfo(getClass(), "query", criteriaResults.getQuery());
        }

        result.setRecords(criteriaResults.getResults()
                                         .stream()
                                         .map(RecordRef::new)
                                         .collect(Collectors.toList()));
        result.setTotalCount(criteriaResults.getTotalCount());
        result.setHasMore(criteriaResults.hasMore());

        return result;
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
