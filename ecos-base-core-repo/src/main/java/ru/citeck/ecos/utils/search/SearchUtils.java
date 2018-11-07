package ru.citeck.ecos.utils.search;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchUtils {

    private SearchService searchService;

    @Autowired
    public SearchUtils(SearchService searchService) {
        this.searchService = searchService;
    }

    public SearchResult<NodeRef> query(SearchParameters params) {
        return query(params, searchService);
    }

    public static SearchResult<NodeRef> query(SearchParameters params, SearchService searchService) {
        ResultSet resultSet = null;
        try {
            resultSet = searchService.query(params);
            return new SearchResult<>(resultSet.getNodeRefs(),
                                      resultSet.hasMore(),
                                      resultSet.getNumberFound());
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }
}
