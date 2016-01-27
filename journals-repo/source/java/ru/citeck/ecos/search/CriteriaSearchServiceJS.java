package ru.citeck.ecos.search;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class CriteriaSearchServiceJS extends BaseScopableProcessorExtension {

    private SearchCriteriaParser parser;
    private CriteriaSearchService searchService;

    public Map<String, Object> query(Object query, String language) {
        CriteriaSearchResults results = searchService.query(parser.parse(query), language);

        Map<String, Object> result = new HashMap<>();
        result.put("query", results.getQuery());
        result.put("nodes", results.getResults());
        result.put("hasMore", results.hasMore());
        result.put("criteria", results.getCriteria());
        result.put("totalCount", results.getTotalCount());

        return result;
    }

    public void setParser(SearchCriteriaParser parser) {
        this.parser = parser;
    }

    public void setSearchService(CriteriaSearchService searchService) {
        this.searchService = searchService;
    }
}
