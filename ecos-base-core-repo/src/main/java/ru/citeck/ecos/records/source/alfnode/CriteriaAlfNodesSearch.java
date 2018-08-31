package ru.citeck.ecos.records.source.alfnode;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.query.*;
import ru.citeck.ecos.search.*;

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
    public DaoRecordsResult queryRecords(RecordsQuery query, Long afterDbId) {

        SearchCriteria criteria = criteriaParser.parse(query.getQuery());
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

        DaoRecordsResult result = new DaoRecordsResult();

        result.setRecords(criteriaResults.getResults()
                                         .stream()
                                         .map(Object::toString)
                                         .collect(Collectors.toList()));
        result.setTotalCount(criteriaResults.getTotalCount());
        result.setHasMore(criteriaResults.hasMore());
        result.setQuery(query);

        return result;
    }

    @Override
    public String getLanguage() {
        return LANGUAGE;
    }
}
