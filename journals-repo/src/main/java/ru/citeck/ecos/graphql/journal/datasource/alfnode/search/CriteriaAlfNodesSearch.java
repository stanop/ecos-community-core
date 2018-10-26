package ru.citeck.ecos.graphql.journal.datasource.alfnode.search;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlSortBy;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.AlfNodesDataSource;
import ru.citeck.ecos.graphql.journal.record.RecordsUtils;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.search.*;

@Component
public class CriteriaAlfNodesSearch implements AlfNodesSearch {

    public static final String LANGUAGE = "criteria";

    private CriteriaSearchService searchService;
    private SearchCriteriaParser criteriaParser;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private RecordsUtils recordsUtils;

    @Autowired
    public CriteriaAlfNodesSearch(CriteriaSearchService searchService,
                                  SearchCriteriaParser criteriaParser,
                                  ServiceRegistry serviceRegistry,
                                  AlfNodesDataSource nodesDataSource,
                                  RecordsUtils recordsUtils) {

        this.searchService = searchService;
        this.criteriaParser = criteriaParser;
        this.nodeService = serviceRegistry.getNodeService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.recordsUtils = recordsUtils;

        nodesDataSource.register(this);
    }

    @Override
    public JGqlRecordsConnection query(GqlContext context, String query, JGqlPageInfoInput pageInfo) {

        JGqlRecordsConnection result = new JGqlRecordsConnection();
        result.pageInfo().set(pageInfo);

        SearchCriteria criteria = criteriaParser.parse(query);

        if (criteria.getTriplets().size() == 0) {
            return result;
        }

        criteria.setSkip(pageInfo.getSkipCount());
        criteria.setLimit(pageInfo.getMaxItems());

        boolean afterIdMode = false;
        String afterIdSortField = "";
        String afterId = pageInfo.getAfterId();

        if (afterId != null) {

            afterIdSortField = ContentModel.PROP_NODE_DBID.toPrefixString(namespaceService);
            criteria.addSort(afterIdSortField, SortOrder.ASCENDING);

            Long dbid = recordsUtils.getRecordDbId(afterId);
            String predicate = SearchPredicate.NUMBER_GREATER_THAN.getValue();
            criteria.addCriteriaTriplet(afterIdSortField, predicate, String.valueOf(dbid));

            afterIdMode = true;
        }

        for (JGqlSortBy sortBy : pageInfo.getSortBy()) {
            if (!afterIdMode || !sortBy.getAttribute().equals(afterIdSortField)) {
                criteria.addSort(sortBy.getAttribute(), sortBy.getOrder());
            }
        }

        CriteriaSearchResults criteriaResults = searchService.query(criteria, SearchService.LANGUAGE_FTS_ALFRESCO);

        result.pageInfo().setHasNextPage(criteriaResults.hasMore());
        result.setRecords(recordsUtils.wrapToAttValue(context, criteriaResults.getResults()));
        result.setTotalCount(criteriaResults.getTotalCount());

        return result;
    }

    @Override
    public String getLanguage() {
        return LANGUAGE;
    }
}
