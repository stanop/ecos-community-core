package ru.citeck.ecos.records.source.alfnode;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.query.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchServiceAlfNodesSearch {

    private static final String FROM_DB_ID_FTS_QUERY = "(%s) AND @sys\\:node\\-dbid:[%d TO MAX]";
    private static final List<String> AFTER_ID_SUPPORT = Arrays.asList(
            SearchService.LANGUAGE_FTS_ALFRESCO,
            SearchService.LANGUAGE_LUCENE
    );

    private SearchService searchService;
    private NamespaceService namespaceService;

    @Autowired
    public SearchServiceAlfNodesSearch(SearchService searchService,
                                       AlfNodesRecordsDAO recordsSource,
                                       NamespaceService namespaceService) {

        this.searchService = searchService;
        this.namespaceService = namespaceService;

        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_FTS_ALFRESCO));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_STRICT));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_LUCENE));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_ALFRESCO));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_CMIS));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_XPATH));
    }

    private DaoRecordsResult queryRecordsImpl(RecordsQuery recordsQuery, Long afterDbId) {

        String query = recordsQuery.getQuery();

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(recordsQuery.getMaxItems());
        searchParameters.setBulkFetchEnabled(false);
        searchParameters.setLanguage(recordsQuery.getLanguage());
        searchParameters.setQueryConsistency(recordsQuery.getConsistency());

        boolean afterIdMode = false;
        String afterIdSortField = "";

        if (afterDbId != null) {

            if (AFTER_ID_SUPPORT.contains(recordsQuery.getLanguage())) {

                query = String.format(FROM_DB_ID_FTS_QUERY, query, afterDbId);
                afterIdSortField = "@" + ContentModel.PROP_NODE_DBID.toPrefixString(namespaceService);

                searchParameters.addSort(afterIdSortField, true);

                afterIdMode = true;
            } else {
                throw new IllegalArgumentException("Page parameter afterId is not supported " +
                                                   "by language " + recordsQuery.getLanguage() +
                                                   ". query: " + recordsQuery);
            }
        } else {
            searchParameters.setSkipCount(recordsQuery.getSkipCount());
        }

        searchParameters.setQuery(query);

        for (SortBy sortBy : recordsQuery.getSortBy()) {
            String field = "@" + sortBy.getAttribute();
            if (!afterIdMode || !afterIdSortField.equals(field)) {
                searchParameters.addSort(field, sortBy.isAscending());
            }
        }

        ResultSet resultSet = null;
        try {

            resultSet = searchService.query(searchParameters);

            DaoRecordsResult result = new DaoRecordsResult();
            result.setRecords(resultSet.getNodeRefs()
                                       .stream()
                                       .map(Object::toString)
                                       .collect(Collectors.toList()));
            result.setQuery(recordsQuery);
            result.setHasMore(resultSet.hasMore());
            result.setTotalCount(resultSet.getNumberFound());

            return result;

        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Nodes search failed. Query: '" + recordsQuery + "'", e);
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
        public DaoRecordsResult queryRecords(RecordsQuery query, Long afterDbId) {
            return queryRecordsImpl(query, afterDbId);
        }

        @Override
        public String getLanguage() {
            return language;
        }
    }
}
