package ru.citeck.ecos.search;

import org.alfresco.service.cmr.search.SearchService;

public class FtsAlfrescoQuery implements SearchQueryBuilder {

    private FtsAlfrescoQueryMigration ftsAlfrescoQueryMigration;

    @Override
    public String buildQuery(SearchCriteria criteria) {
        return ftsAlfrescoQueryMigration.buildQuery(criteria);
    }

    @Override
    public boolean supports(String language) {
        return SearchService.LANGUAGE_FTS_ALFRESCO.equals(language);
    }

    public void setFtsAlfrescoQueryMigration(FtsAlfrescoQueryMigration ftsAlfrescoQueryMigration) {
        this.ftsAlfrescoQueryMigration = ftsAlfrescoQueryMigration;
    }
}
