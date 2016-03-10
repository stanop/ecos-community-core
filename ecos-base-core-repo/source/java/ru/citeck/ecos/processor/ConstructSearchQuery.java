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

import org.alfresco.service.cmr.repository.ContentReader;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.search.SearchQueryBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Constructs search query by criteria from DataBundle's input stream.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class ConstructSearchQuery extends AbstractDataBundleLine {

    private SearchQueryBuilder builder;

    private SearchCriteriaParser parser;

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        ContentReader contentReader = helper.getContentReader(input);
        String criteria = contentReader.getContentString();
        SearchCriteria searchCriteria = parser.parse(evaluateExpression(criteria, model));
        HashMap<String, Object> newModel = new HashMap<String, Object>();
        newModel.putAll(model);

        newModel.put("searchCriteria", searchCriteria);
        newModel.put("criteria", searchCriteria.toMap());
        String query = builder.buildQuery(searchCriteria);
        newModel.put("query", query);
        return new DataBundle(newModel);
    }

    public void setBuilder(SearchQueryBuilder builder) {
        this.builder = builder;
    }

    public void setParser(SearchCriteriaParser parser) {
        this.parser = parser;
    }
}
