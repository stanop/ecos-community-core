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

import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Query builder for Lucene search
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class LuceneQuery implements SearchQueryBuilder {

    private static final String WILD = "*";

    private static final String SEPARATOR = ":";

    private static final String QUOTE = "\"";

    private static final String FROM_MIN = "MIN TO ";

    private static final String TO_MAX = " TO MAX";

    private static final String AND = " AND ";

    private static final String OR = " OR ";

    private static final String NOT = "NOT ";

    private StringBuilder query;
    
    private NamespaceService namespaceService;

    private AssociationIndexPropertyRegistry associationIndexPropertyRegistry;

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    public void setAssociationIndexPropertyRegistry(AssociationIndexPropertyRegistry registry) {
        this.associationIndexPropertyRegistry = registry;
    }
    
    @Override
    public String buildQuery(SearchCriteria criteria) {
        query = new StringBuilder();
        Iterator<CriteriaTriplet> iterator = criteria.getTripletsIterator();
        while (iterator.hasNext()) {
            CriteriaTriplet criteriaTriplet = iterator.next();
            buildSearchTerm(criteriaTriplet);
            if (iterator.hasNext()) {
                query.append(AND);
            }
        }
        return query.toString();
    }

    @Override
    public boolean supports(String language) {
        return SearchService.LANGUAGE_LUCENE.equals(language);
    }

    private void buildSearchTerm(CriteriaTriplet triplet) {
        SearchPredicate criterion = SearchPredicate.forName(triplet.getPredicate());
        String field = buildField(triplet.getField());
        switch (criterion) {
        case ASSOC_CONTAINS:
        case ASSOC_NOT_CONTAINS:
        case ASSOC_NOT_EMPTY:
        case ASSOC_EMPTY:
            field = buildField(getAssocIndexProp(triplet.getField()));
            break;
        default:
	        break;
       }
        
        String value = triplet.getValue();
        switch (criterion) {
            case STRING_CONTAINS:
                buildEqualsTerm(field, WILD + value + WILD);
                break;
            case STRING_NOT_EQUALS:
            case NUMBER_NOT_EQUALS:
            case DATE_NOT_EQUALS:
            case TYPE_NOT_EQUALS:
            case ASPECT_NOT_EQUALS:
                query.append(NOT);
            case STRING_EQUALS:
            case NUMBER_EQUALS:
            case DATE_EQUALS:
            case TYPE_EQUALS:
            case ASPECT_EQUALS:
            case PARENT_EQUALS:
            case PATH_EQUALS:
            case LIST_EQUALS:
            case LIST_NOT_EQUALS:
                buildEqualsTerm(field, value);
                break;
            case STRING_STARTS_WITH:
                buildEqualsTerm(field, value + WILD);
                break;
            case STRING_ENDS_WITH:
                buildEqualsTerm(field, WILD + value);
                break;
            case DATE_NOT_EMPTY:
            case BOOLEAN_NOT_EMPTY:
            case NODEREF_NOT_EMPTY:
            case ASSOC_NOT_EMPTY:
            case FLOAT_NOT_EMPTY:
            case INT_NOT_EMPTY:
            case STRING_NOT_EMPTY:
                buildEmptyCheckTerm(field, false);
                break;
            case DATE_EMPTY:
            case BOOLEAN_EMPTY:
            case NODEREF_EMPTY:
            case ASSOC_EMPTY:
                buildNullCheckTerm(field, true);
                break;
            case STRING_EMPTY:
                buildEmptyCheckTerm(field, true);
                break;
            case NUMBER_LESS_THAN:
                buildLessThanTerm(field, value, false);
                break;
            case NUMBER_GREATER_THAN:
                buildGreaterThanTerm(field, value, false);
                break;
            case NUMBER_LESS_OR_EQUAL:
            case DATE_LESS_OR_EQUAL:
            case DATE_LESS_THAN:
                buildLessThanTerm(field, value, true);
                break;
            case NUMBER_GREATER_OR_EQUAL:
            case DATE_GREATER_OR_EQUAL:
                buildGreaterThanTerm(field, value, true);
                break;
            case BOOLEAN_TRUE:
                buildEqualsTerm(field, "true");
                break;
            case BOOLEAN_FALSE:
                buildEqualsTerm(field, "false");
                break;
            case ANY:
                buildEqualsTerm(field, WILD);
                break;
            case NODEREF_NOT_CONTAINS:
            case ASSOC_NOT_CONTAINS:
            case QNAME_NOT_CONTAINS:
                query.append(NOT);
            case NODEREF_CONTAINS:
            case ASSOC_CONTAINS:
            case QNAME_CONTAINS:
            	buildListContainsTerm(field, value);
                break;
            case PATH_CHILD:
                buildEqualsTerm(field, value + "/*");
                break;
            case PATH_DESCENDANT:
                buildEqualsTerm(field, value + "//*");
                break;
            case ID_EQUALS:
                break;
        }
    }

    private String buildField(String field) {
        try {
            return FieldType.forName(field).toString();
        } catch (IllegalArgumentException e) {
            return "@" + escape(field);
        }
    }

    private String escape(String string) {
        return string.contains(SEPARATOR) ?
                new StringBuilder(string).insert(string.indexOf(SEPARATOR), "\\").toString() :
                string;
    }

    private void buildEqualsTerm(String field, String value) {
        query.append(field).append(SEPARATOR).append(QUOTE).append(value).append(QUOTE);
    }

    private void buildRangeTerm(String field, String value, boolean inclusive, boolean lessThan) {
        StringBuilder range = new StringBuilder();
        range.append(inclusive ? "[" : "{");
        if (lessThan) {
            range.append(FROM_MIN);
        }
        range.append(value);
        if (!lessThan) {
            range.append(TO_MAX);
        }
        range.append(inclusive ? "]" : "}");
        query.append(field).append(SEPARATOR).append(range);
    }

    private void buildLessThanTerm(String field, String value, boolean inclusive) {
        buildRangeTerm(field, value, inclusive, true);
    }

    private void buildGreaterThanTerm(String field, String value, boolean inclusive) {
        buildRangeTerm(field, value, inclusive, false);
    }

    private void buildNullCheckTerm(String value, boolean isNull) {
        buildEqualsTerm(isNull ? "ISNULL" : "ISNOTNULL", getPropertyName(value));
    }

    private void buildEmptyCheckTerm(String field, boolean isEmpty) {
        query.append("(");
        buildNullCheckTerm(field, isEmpty);
        if (isEmpty) query.append(OR);
        else query.append(AND).append(NOT);
        buildEqualsTerm(field, "");
        query.append(")");
    }
    
    protected String getAssocIndexProp(String assocName) {
        return associationIndexPropertyRegistry.getAssociationIndexProperty(assocName, namespaceService);
    }

    private void buildListContainsTerm(String field, String value) {
        List<String> listItems = Arrays.asList(value.split("\\,"));
        if (!listItems.isEmpty()) {
            query.append("(");
            Iterator<String> iterator = listItems.iterator();
            while (iterator.hasNext()) {
                String item = iterator.next();
                buildEqualsTerm(field, item.trim());
                if (iterator.hasNext()) {
                    query.append(OR);
                }
            }
            query.append(")");
        }
    }

    private String getPropertyName(String field) {
        return field.replace("@", "").replace("\\", "");
    }

}
