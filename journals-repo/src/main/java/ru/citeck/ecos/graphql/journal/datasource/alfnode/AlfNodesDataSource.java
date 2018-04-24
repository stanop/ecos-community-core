package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JournalRecordGql;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
                                               String after, Integer first) {
        int skipCount = 0;
        if (after != null) {
            skipCount = Base64.decodeInteger(Base64.decodeBase64(after)).intValue();
        }

        SearchCriteria criteria = criteriaParser.parse(query);
        criteria.setSkip(skipCount);
        criteria.setLimit(first);

        CriteriaSearchResults criteriaResults = criteriaSearchService.query(criteria, language);

        List<JournalRecordGql> records = new ArrayList<>();

        for (NodeRef nodeRef : criteriaResults.getResults()) {
            context.getNode(nodeRef)
                   .ifPresent(n -> records.add(new AlfNodeRecord(n, context)));
        }

        JournalRecordsConnection result = new JournalRecordsConnection();

        result.setRecords(records);

        JournalGqlPageInfo paging = new JournalGqlPageInfo();

        byte[] endCursorBytes = Base64.encodeInteger(BigInteger.valueOf(skipCount + first));
        paging.setEndCursor(Base64.encodeBase64String(endCursorBytes));
        paging.setHasNextPage(criteriaResults.hasMore());
        result.setPageInfo(paging);

        return result;
    }

    @Override
    public JournalAttributeInfo getAttributeInfo(String attributeName) {

        

        dictionaryService.getAssociation(attributeName)

        return null;
    }
}
