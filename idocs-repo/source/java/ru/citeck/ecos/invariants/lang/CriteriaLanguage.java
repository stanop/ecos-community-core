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
package ru.citeck.ecos.invariants.lang;

import java.util.Map;

import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.NamespaceService;

import ru.citeck.ecos.invariants.AbstractInvariantLanguage;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.CriteriaTriplet;
import ru.citeck.ecos.search.SearchCriteria;

public class CriteriaLanguage extends AbstractInvariantLanguage {

    private static final String NAME = "criteria";
    private CriteriaSearchService criteriaSearchService;
    private NamespaceService namespaceService;
    private TemplateService templateService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isValueSupported(Object value) {
        return value instanceof SearchCriteria;
    }
    
    @Override
    public Object evaluate(Object expression, Map<String, Object> model) {
        SearchCriteria criteriaExpression = (SearchCriteria) expression;
        SearchCriteria criteria = new SearchCriteria(namespaceService);
        try {
            for(CriteriaTriplet criterion : criteriaExpression.getTriplets()) {
                criteria.addCriteriaTriplet(
                        criterion.getField(), 
                        criterion.getPredicate(), 
                        templateService.processTemplateString("freemarker", criterion.getValue(), model));
            }
            CriteriaSearchResults searchResponse = criteriaSearchService.query(criteria, "lucene");
            return searchResponse.getResults();
        } catch(RuntimeException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return NAME + " invariant language";
    }

    public void setCriteriaSearchService(CriteriaSearchService criteriaSearchService) {
        this.criteriaSearchService = criteriaSearchService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    
}
