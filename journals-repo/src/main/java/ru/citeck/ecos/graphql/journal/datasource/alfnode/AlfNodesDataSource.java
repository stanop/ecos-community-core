package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.math.BigInteger;
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

    private GqlContext context;

    @Override
    public JournalRecordsConnection getRecords(GqlContext context,
                                               String query, String language,
                                               String after, Integer first) {
        this.context = context;

        if (language == null) {
            language = SearchService.LANGUAGE_FTS_ALFRESCO;
        }

        int skipCount = 0;
        if (after != null) {
            skipCount = Base64.decodeInteger(Base64.decodeBase64(after)).intValue();
        }

        SearchCriteria criteria = criteriaParser.parse(query);
        criteria.setSkip(skipCount);
        criteria.setLimit(first);

        CriteriaSearchResults criteriaResults = criteriaSearchService.query(criteria, language);

        List<JournalAttributeValueGql> records = new ArrayList<>();

        for (NodeRef nodeRef : criteriaResults.getResults()) {
            context.getNode(nodeRef)
                   .ifPresent(n -> records.add(new AlfNodeRecord(n, context)));
        }

        JournalRecordsConnection result = new JournalRecordsConnection();

        result.setTotalCount(criteriaResults.getTotalCount());
        result.setRecords(records);

        JournalGqlPageInfo paging = new JournalGqlPageInfo();
        byte[] endCursorBytes = Base64.encodeInteger(BigInteger.valueOf(skipCount + first));
        paging.setEndCursor(Base64.encodeBase64String(endCursorBytes));
        paging.setHasNextPage(criteriaResults.hasMore());
        result.setPageInfo(paging);

        return result;
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        QName qName = QName.resolveToQName(namespaceService, attributeName);
        return Optional.of(new AlfNodeAttributeInfo(qName, dictionaryService));
    }
}
