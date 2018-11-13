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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
@JsonSerialize(using = SearchCriteria.Serializer.class)
public class SearchCriteria {

    private final List<CriteriaTriplet> triplets;

    private Integer skip;

    private Integer limit;

    private Map<String, Boolean> sort;

    private NamespacePrefixResolver namespaceService;

    public SearchCriteria(NamespacePrefixResolver namespaceService) {
        triplets = new ArrayList<>();
        sort = new LinkedHashMap<>();
        skip = null;
        limit = null;
        this.namespaceService = namespaceService;
    }

    public SearchCriteria(SearchCriteria other) {
        this(other.namespaceService);
        other.triplets.forEach(t ->
                triplets.add(new CriteriaTriplet(t.getField(), t.getPredicate(), t.getValue())));
        skip = other.skip;
        limit = other.limit;
        sort.putAll(other.sort);
    }

    public SearchCriteria addCriteriaTriplet(String field, String predicate, String value) {
        triplets.add(new CriteriaTriplet(field, predicate, value));
        return this;
    }

    public SearchCriteria addCriteriaTriplet(FieldType field, SearchPredicate predicate, QName value) {
        triplets.add(new CriteriaTriplet(field.toString(), predicate.toString(), value.toPrefixString(namespaceService)));
        return this;
    }

    public SearchCriteria addCriteriaTriplet(QName field, SearchPredicate predicate, String value) {
        triplets.add(new CriteriaTriplet(field.toPrefixString(namespaceService), predicate.toString(), value));
        return this;
    }

    public SearchCriteria addCriteriaTriplet(QName field, SearchPredicate predicate, List<?> values) {
        String value = StringUtils.join(values, ",");
        triplets.add(new CriteriaTriplet(field.toPrefixString(namespaceService), predicate.toString(), value));
        return this;
    }

    public List<CriteriaTriplet> getTriplets() {
        return triplets;
    }

    public Iterator<CriteriaTriplet> getTripletsIterator() {
        return triplets.iterator();
    }

    public Integer getSkip() {
        return skip;
    }

    public SearchCriteria setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public SearchCriteria setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public boolean isSkipSet() {
        return skip != null;
    }

    public boolean isLimitSet() {
        return limit != null;
    }

    public Map<String, Boolean> getSort() {
        return sort;
    }

    public SearchCriteria addSort(String field, String order) {
        sort.put(field, SortOrder.forName(order).toBoolean());
        return this;
    }
    
    public SearchCriteria addSort(String field, SortOrder order) {
    	sort.put(field, order.toBoolean());
        return this;
    }

    public SearchCriteria addSort(QName field, SortOrder order) {
        sort.put(field.toPrefixString(namespaceService), order.toBoolean());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchCriteria)) return false;

        SearchCriteria that = (SearchCriteria) o;

        if (limit != null ? !limit.equals(that.limit) : that.limit != null) return false;
        if (skip != null ? !skip.equals(that.skip) : that.skip != null) return false;
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) return false;
        return triplets.equals(that.triplets);
    }

    @Override
    public int hashCode() {
        int result = triplets.hashCode();
        result = 31 * result + (skip != null ? skip.hashCode() : 0);
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        return result;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("skip", skip);
        map.put("limit", limit);
        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SearchCriteria (");
        for (CriteriaTriplet triplet : triplets) {
            sb.append("[")
                    .append(triplet.getField())
                    .append(" ")
                    .append(triplet.getPredicate())
                    .append(" ")
                    .append(triplet.getValue())
                    .append("] ");
        }
        sb.append(")\n");
        return sb.toString();
    }

    public static class Serializer extends StdSerializer<SearchCriteria> {

        public Serializer() {
            super(SearchCriteria.class);
        }

        @Override
        public void serialize(SearchCriteria value,
                              JsonGenerator jgen,
                              SerializerProvider provider) throws IOException {

            jgen.writeStartObject();

            String fieldPrefix = SearchCriteriaParser.FIELD_KEY + SearchCriteriaParser.SEPARATOR;
            String predicatePrefix = SearchCriteriaParser.PREDICATE_KEY + SearchCriteriaParser.SEPARATOR;
            String valuePrefix = SearchCriteriaParser.VALUE_KEY + SearchCriteriaParser.SEPARATOR;

            int idx = 0;

            for (CriteriaTriplet triplet : value.getTriplets()) {
                jgen.writeStringField(fieldPrefix + idx, triplet.getField());
                jgen.writeStringField(predicatePrefix + idx, triplet.getPredicate());
                jgen.writeStringField(valuePrefix + idx, triplet.getValue());
                idx++;
            }

            jgen.writeEndObject();
        }
    }
}
