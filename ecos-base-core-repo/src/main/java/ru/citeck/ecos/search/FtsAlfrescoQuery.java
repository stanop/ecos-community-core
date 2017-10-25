package ru.citeck.ecos.search;

import org.alfresco.service.cmr.search.SearchService;

public class FtsAlfrescoQuery implements SearchQueryBuilder {

    private LuceneQuery luceneQuery;

    @Override
    public String buildQuery(SearchCriteria criteria) {
        return luceneQuery.buildQuery(criteria);
    }

    @Override
    public boolean supports(String language) {
        return SearchService.LANGUAGE_FTS_ALFRESCO.equals(language);
    }

    public void setLuceneQuery(LuceneQuery luceneQuery) {
        this.luceneQuery = luceneQuery;
    }
}
