package ru.citeck.ecos.records.source.alfnode;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.*;
import ru.citeck.ecos.records.source.alfnode.AlfNodesSearch.AfterIdType;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class SearchServiceAlfNodesSearch {

    private static final Log logger = LogFactory.getLog(SearchServiceAlfNodesSearch.class);

    private static final String FROM_DB_ID_FTS_QUERY = "(%s) AND @sys\\:node\\-dbid:[%d TO MAX]";

    private SearchService searchService;
    private NamespaceService namespaceService;

    @Autowired
    public SearchServiceAlfNodesSearch(SearchService searchService,
                                       AlfNodesRecordsDAO recordsSource,
                                       NamespaceService namespaceService) {

        this.searchService = searchService;
        this.namespaceService = namespaceService;

        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_FTS_ALFRESCO, AfterIdType.DB_ID));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_LUCENE, AfterIdType.DB_ID));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_ALFRESCO, AfterIdType.DB_ID));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, AfterIdType.DB_ID));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_SOLR_CMIS, AfterIdType.CREATED));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO, AfterIdType.CREATED));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_CMIS_STRICT, AfterIdType.CREATED));
        recordsSource.register(new SearchWithLanguage(SearchService.LANGUAGE_XPATH));
    }

    private RecordsResult queryRecordsImpl(RecordsQuery recordsQuery, Long afterDbId, Date afterCreated) {

        String query = recordsQuery.getQuery();

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(recordsQuery.getMaxItems());
        searchParameters.setBulkFetchEnabled(false);
        searchParameters.setLanguage(recordsQuery.getLanguage());
        searchParameters.setQueryConsistency(recordsQuery.getConsistency());

        boolean afterIdMode = false;
        String afterIdSortField = "";
        boolean ignoreQuerySort = false;

        if (afterDbId != null) {

            query = String.format(FROM_DB_ID_FTS_QUERY, query, afterDbId);
            afterIdSortField = "@" + ContentModel.PROP_NODE_DBID.toPrefixString(namespaceService);

            searchParameters.addSort(afterIdSortField, true);

            afterIdMode = true;

        } else if (afterCreated != null && recordsQuery.getLanguage().startsWith("cmis-")) {

            query = query.replaceAll("(?i)order by.+", "");
            if (!query.contains("where") && !query.contains("WHERE")) {
                query += " where";
            } else {
                query += " and";
            }
            query += " cmis:creationDate > '" + ISO8601Utils.format(afterCreated) + "' order by cmis:creationDate";
            ignoreQuerySort = true;

        } else {
            searchParameters.setSkipCount(recordsQuery.getSkipCount());
        }

        searchParameters.setQuery(query);

        if (!ignoreQuerySort) {
            for (SortBy sortBy : recordsQuery.getSortBy()) {
                String field = "@" + sortBy.getAttribute();
                if (!afterIdMode || !afterIdSortField.equals(field)) {
                    searchParameters.addSort(field, sortBy.isAscending());
                }
            }
        }

        ResultSet resultSet = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Execute query with parameters: " + searchParameters);
            }
            resultSet = searchService.query(searchParameters);

            RecordsResult result = new RecordsResult();
            result.setRecords(resultSet.getNodeRefs()
                                       .stream()
                                       .map(RecordRef::new)
                                       .collect(Collectors.toList()));
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
        private final AfterIdType afterIdType;

        SearchWithLanguage(String language, AfterIdType afterIdType) {
            this.language = language;
            this.afterIdType = afterIdType;
        }

        SearchWithLanguage(String language) {
            this(language, null);
        }

        @Override
        public RecordsResult queryRecords(RecordsQuery query, Long afterDbId, Date afterCreated) {
            return queryRecordsImpl(query, afterDbId, afterCreated);
        }

        @Override
        public AfterIdType getAfterIdType() {
            return afterIdType;
        }

        @Override
        public String getLanguage() {
            return language;
        }
    }
}
