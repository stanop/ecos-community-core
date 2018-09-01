package ru.citeck.ecos.graphql.journal.datasource.alfnode.search;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlSortBy;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.AlfNodesDataSource;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.record.RecordsUtils;

import java.util.Arrays;
import java.util.List;

@Component
public class SearchServiceAlfNodesSearch {

    private static final String FROM_DB_ID_FTS_QUERY = "(%s) AND @sys\\:node\\-dbid:[%d TO MAX]";
    private static final List<String> AFTER_ID_SUPPORT = Arrays.asList(
            SearchService.LANGUAGE_FTS_ALFRESCO,
            SearchService.LANGUAGE_LUCENE
    );

    private SearchService searchService;
    private RecordsUtils recordsUtils;
    private NamespaceService namespaceService;

    @Autowired
    public SearchServiceAlfNodesSearch(SearchService searchService,
                                       RecordsUtils recordsUtils,
                                       AlfNodesDataSource dataSource,
                                       NamespaceService namespaceService) {

        this.searchService = searchService;
        this.recordsUtils = recordsUtils;
        this.namespaceService = namespaceService;

        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_FTS_ALFRESCO));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_STRICT));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_LUCENE));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_ALFRESCO));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_CMIS));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO));
        dataSource.register(new SearchWithLanguage(SearchService.LANGUAGE_XPATH));
    }

    private JGqlRecordsConnection queryImpl(GqlContext context,
                                            String language,
                                            String query,
                                            JGqlPageInfoInput pageInfo) {

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQueryConsistency(QueryConsistency.EVENTUAL);
        searchParameters.setMaxItems(pageInfo.getMaxItems());
        searchParameters.setBulkFetchEnabled(false);
        searchParameters.setLanguage(language);

        String afterId = pageInfo.getAfterId();
        boolean afterIdMode = false;
        String afterIdSortField = "";

        if (afterId != null) {

            if (AFTER_ID_SUPPORT.contains(language)) {

                Long dbid = recordsUtils.getRecordDbId(afterId) + 1;
                query = String.format(FROM_DB_ID_FTS_QUERY, query, dbid);
                afterIdSortField = "@" + ContentModel.PROP_NODE_DBID.toPrefixString(namespaceService);

                searchParameters.addSort(afterIdSortField, true);

                afterIdMode = true;
            } else {
                throw new IllegalArgumentException("Page parameter afterId is not supported " +
                                                   "by language " + language + ". query: " + query);
            }
        } else {
            searchParameters.setSkipCount(pageInfo.getSkipCount());
        }

        searchParameters.setQuery(query);

        for (JGqlSortBy sortBy : pageInfo.getSortBy()) {
            String field = "@" + sortBy.getAttribute();
            if (!afterIdMode || !afterIdSortField.equals(field)) {
                searchParameters.addSort(field, sortBy.isAscending());
            }
        }

        ResultSet resultSet = null;
        try {
            resultSet = searchService.query(searchParameters);

            JGqlRecordsConnection records = new JGqlRecordsConnection();
            records.setRecords(recordsUtils.wrapToAttValue(context, resultSet.getNodeRefs()));
            records.pageInfo().setHasNextPage(resultSet.hasMore());
            records.pageInfo().set(pageInfo);

            return records;

        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Nodes search failed. Query: '" + query + "'", e);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private class SearchWithLanguage implements AlfNodesSearch {

        private final String language;

        SearchWithLanguage(String language) {
            this.language = language;
        }

        @Override
        public JGqlRecordsConnection query(GqlContext context, String query, JGqlPageInfoInput pageInfo) {
            return queryImpl(context, language, query, pageInfo);
        }

        @Override
        public String getLanguage() {
            return language;
        }
    }
}
