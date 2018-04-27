package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JournalGqlSortBy;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlfNodesDataSource implements JournalDataSource {

    @Autowired
    private CriteriaSearchService criteriaSearchService;
    @Autowired
    private SearchCriteriaParser criteriaParser;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private NamespaceService namespaceService;

    @Override
    public JournalRecordsConnection getRecords(GqlContext context,
                                               String query, String language,
                                               JournalGqlPageInfoInput pageInfo) {

        if (language == null) {
            language = SearchService.LANGUAGE_FTS_ALFRESCO;
        }

        SearchCriteria criteria = criteriaParser.parse(query);
        criteria.setSkip(pageInfo.getSkipCount());
        criteria.setLimit(pageInfo.getMaxItems());

        for (JournalGqlSortBy sortBy : pageInfo.getSortBy()) {
            criteria.addSort(sortBy.getAttribute(), sortBy.getOrder());
        }

        CriteriaSearchResults criteriaResults = criteriaSearchService.query(criteria, language);

        List<JournalAttributeValueGql> records = new ArrayList<>();

        for (NodeRef nodeRef : criteriaResults.getResults()) {
            context.getNode(nodeRef)
                   .ifPresent(n -> records.add(new AlfNodeRecord(n, context)));
        }

        JournalRecordsConnection result = new JournalRecordsConnection();

        result.setTotalCount(criteriaResults.getTotalCount());
        result.setRecords(records);

        JournalGqlPageInfo outPageInfo = new JournalGqlPageInfo();
        outPageInfo.setHasNextPage(criteriaResults.hasMore());
        outPageInfo.setSkipCount(pageInfo.getSkipCount());
        outPageInfo.setMaxItems(pageInfo.getMaxItems());
        result.setPageInfo(outPageInfo);

        return result;
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        QName qName = QName.resolveToQName(namespaceService, attributeName);
        return Optional.of(new AlfNodeAttributeInfo(qName, dictionaryService));
    }
}
