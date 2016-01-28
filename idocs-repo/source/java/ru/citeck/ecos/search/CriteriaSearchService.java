/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.search;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * Service for search by criteria.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class CriteriaSearchService {

    private SearchService searchService;

    private List<SearchQueryBuilder> queryBuilders;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private AssociationIndexPropertyRegistry associationIndexPropertyRegistry;
    private SortFieldChanger sortFieldChanger;
    
    /**
     * Search using given SearchCriteria with specified query language.
     *
     * @param criteria criteria for search
     * @param language search query language
     * @return query results as ResultSet
     */
    public CriteriaSearchResults query(SearchCriteria criteria, String language) {
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria can't be null");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Language can't be null or empty");
        }
        SearchQueryBuilder queryBuilder = getMatchingQueryBuilder(language);
        if(queryBuilder == null) {
            throw new IllegalArgumentException("Unsupported query language");
        }
        String query = queryBuilder.buildQuery(criteria);
        SearchParameters parameters = new SearchParameters();
        parameters.setLanguage(language);
        parameters.setQuery(query);
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        if (criteria.isLimitSet()) {
            // as ResultSet.hasMore() throws UnsupportedOperationException
            // we submit maxItems+1 to simulate it
            parameters.setMaxItems(criteria.getLimit() + 1);
        }
        if (criteria.isSkipSet()) {
            parameters.setSkipCount(criteria.getSkip());
        }
        Map<String, Boolean> sortCriteria = criteria.getSort();
        for (String field : sortCriteria.keySet()) {
            if(sortFieldChanger != null) {
                field = sortFieldChanger.getSortField(field);
            }
            QName fieldQName = QName.resolveToQName(namespaceService, field);
            if(dictionaryService.getProperty(fieldQName) != null) {
                parameters.addSort("@" + field, sortCriteria.get(field));
                continue;
            }
            if(dictionaryService.getAssociation(fieldQName) != null) {
                QName indexField = associationIndexPropertyRegistry.getAssociationIndexProperty(fieldQName);
                parameters.addSort("@" + indexField, sortCriteria.get(field));
                continue;
            }
            throw new IllegalArgumentException("Field " + field + " is neither property, nor association");
        }
        ResultSet resultSet = null;
        List<NodeRef> results = null;
        try {
            resultSet = searchService.query(parameters);
            results = resultSet.getNodeRefs();
        } finally {
            if(resultSet != null) {
                resultSet.close();
            }
        }
        
        boolean hasMore = false;
        if(criteria.isLimitSet()) {
            hasMore = results.size() > criteria.getLimit();
            if(hasMore) {
                results = results.subList(0, criteria.getLimit());
            }
        }
        
        return new CriteriaSearchResults.Builder()
                .criteria(criteria)
                .results(results)
                .hasMore(hasMore)
                .query(query)
                .build();
    }
    
    private SearchQueryBuilder getMatchingQueryBuilder(String language) {
        for (SearchQueryBuilder queryBuilder : queryBuilders) {
            if (queryBuilder.supports(language)) {
                return queryBuilder;
            }
        }
        return null;
    }
    
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setQueryBuilders(List<SearchQueryBuilder> queryBuilders) {
        this.queryBuilders = queryBuilders;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    public void setAssociationIndexPropertyRegistry(
            AssociationIndexPropertyRegistry associationIndexPropertyRegistry) {
        this.associationIndexPropertyRegistry = associationIndexPropertyRegistry;
    }
    
    public void setSortFieldChanger(SortFieldChanger sortFieldChanger) {
        this.sortFieldChanger = sortFieldChanger;
    }
}
