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
package ru.citeck.ecos.processor;

import org.alfresco.service.cmr.repository.NodeRef;

import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class ExecuteSearchQuery extends AbstractDataBundleLine {

    private String language;

    private CriteriaSearchService searchService;

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        SearchCriteria searchCriteria = (SearchCriteria) model.get("searchCriteria");
        CriteriaSearchResults results = searchService.query(searchCriteria, language);
        List<NodeRef> nodeRefs = results.getResults();

        HashMap<String, Object> newModel = new HashMap<String, Object>();
        newModel.putAll(model);
        newModel.put("language", language);
        newModel.put("hasMore", results.hasMore());
        newModel.put("totalCount", results.getTotalCount());
        newModel.put("nodes", nodeRefs);
        return helper.getDataBundle(helper.getContentReader(input), newModel);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSearchService(CriteriaSearchService searchService) {
        this.searchService = searchService;
    }
}
