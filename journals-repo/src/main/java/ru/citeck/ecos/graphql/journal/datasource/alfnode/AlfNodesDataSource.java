package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalRecordGql;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSourcePaging;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSourceResult;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.util.ArrayList;
import java.util.List;

public class AlfNodesDataSource implements JournalDataSource {

    @Autowired
    private CriteriaSearchService criteriaSearchService;
    @Autowired
    private SearchCriteriaParser criteriaParser;

    @Override
    public JournalDataSourceResult getRecords(GqlContext context, String query, String language) {

        SearchCriteria criteria = criteriaParser.parse(query);
        CriteriaSearchResults criteriaResults = criteriaSearchService.query(criteria, language);

        List<JournalRecordGql> records = new ArrayList<>();

        for (NodeRef nodeRef : criteriaResults.getResults()) {
            context.getNode(nodeRef)
                   .ifPresent(n -> records.add(new AlfNodeRecord(n, context)));
        }

        JournalDataSourceResult result = new JournalDataSourceResult();
        result.setRecords(records);

        JournalDataSourcePaging paging = new JournalDataSourcePaging();
        paging.hasMore = criteriaResults.hasMore();
        paging.totalCount = criteriaResults.getResults().size();
        paging.totalItems = criteriaResults.getTotalCount();
        paging.maxItems = criteria.getLimit();
        paging.skipCount = criteria.getSkip();
        result.setPaging(paging);

        return result;
    }
}
