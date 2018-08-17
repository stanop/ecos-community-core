package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import com.google.common.collect.Lists;
import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.AlfGraphQLServiceImpl;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlRecordsInput;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.search.AlfNodesSearch;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.record.RecordsUtils;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AlfNodesDataSource implements JournalDataSource {

    private static final QName RECORDS_UTILS_QNAME = QName.createQName(null, "recordsUtils");
    private static final QName GRAPHQL_SERVICE_QNAME = QName.createQName(null, "alfGraphQLServiceImpl");

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private RecordsUtils recordsUtils;
    private GraphQLService graphQLService;

    private Map<String, AlfNodesSearch> nodesSearchByLang = new ConcurrentHashMap<>();

    @Autowired
    public AlfNodesDataSource(ServiceRegistry serviceRegistry) {
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        recordsUtils = (RecordsUtils) serviceRegistry.getService(RECORDS_UTILS_QNAME);
        graphQLService = (GraphQLService) serviceRegistry.getService(GRAPHQL_SERVICE_QNAME);
    }

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {

        AlfNodesSearch alfNodesSearch = nodesSearchByLang.get(language);

        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language " + language + " is not supported!");
        }

        return alfNodesSearch.query(context, query, pageInfo);
    }

    @Override
    public RecordsResult queryIds(GqlContext context,
                                  String query,
                                  String language,
                                  JGqlPageInfoInput pageInfo) {

        String validLanguage = StringUtils.isNotBlank(language) ? language : CriteriaAlfNodesSearch.LANGUAGE;
        JGqlRecordsConnection searchResult = getRecords(context, query, validLanguage, pageInfo);
        return constructRecordsResult(pageInfo, searchResult);
    }

    private RecordsResult constructRecordsResult(JGqlPageInfoInput pageInfo, JGqlRecordsConnection searchResult) {
        List<RemoteRef> recordsData;
        Boolean hasNextPage;
        int skipCount;
        int maxItems;
        long totalCount;

        if (searchResult != null && searchResult.records() != null) {
            recordsData = new ArrayList<>(searchResult.records().size());
            for (JGqlAttributeValue attributeValue : searchResult.records()) {
                recordsData.add(new RemoteRef(attributeValue.id()));
            }
        } else {
            recordsData = Collections.emptyList();
        }

        if (searchResult != null && searchResult.pageInfo() != null) {
            hasNextPage = searchResult.pageInfo().isHasNextPage();
            skipCount = searchResult.pageInfo().getSkipCount();
            maxItems = searchResult.pageInfo().getMaxItems();
        } else {
            hasNextPage = pageInfo.getMaxItems() == recordsData.size();
            maxItems = DEFAULT_PAGE_SIZE;
            skipCount = 0;
        }

        if (searchResult != null) {
            totalCount = searchResult.totalCount();
        } else {
            totalCount = recordsData.size();
        }

        return new RecordsResult(recordsData, hasNextPage, totalCount, skipCount, maxItems);
    }

    @Override
    public List<JGqlAttributeValue> convertToGqlValue(GqlContext context,
                                                      List<RemoteRef> remoteRefList) {
        return recordsUtils.wrapRefsToLocalValue(context, remoteRefList);
    }

    @Override
    public ExecutionResult queryMetadata(JournalType journalType,
                                         String gqlQuery,
                                         List<RemoteRef> remoteRefList) {
        List<String> recordIds = new ArrayList<>(remoteRefList.size());
        remoteRefList.forEach(item -> recordIds.add(item.toString()));

        Map<String, Object> params = new HashMap<>();
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_DATASOURCE, journalType.getDataSource());
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_REMOTE_REFS, new JGqlRecordsInput(recordIds));

        return graphQLService.execute(gqlQuery, params);
    }

    @Override
    public boolean isSupportsSplitLoading() {
        return true;
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.of(new AlfNodeAttributeInfo(attributeName, namespaceService, dictionaryService));
    }

    @Override
    public List<String> getDefaultAttributes() {
        return Lists.newArrayList(AlfNodeRecord.ATTR_ASPECTS,
                                  AlfNodeRecord.ATTR_IS_DOCUMENT,
                                  AlfNodeRecord.ATTR_IS_CONTAINER);
    }

    public void register(AlfNodesSearch nodesSearch) {
        nodesSearchByLang.put(nodesSearch.getLanguage(), nodesSearch);
    }
}
