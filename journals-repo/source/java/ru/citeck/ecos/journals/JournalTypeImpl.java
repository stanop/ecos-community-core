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
package ru.citeck.ecos.journals;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.journals.xml.Header;
import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Option;

class JournalTypeImpl implements JournalType {

    private final String id;
    private final Map<String, String> options;
    private final List<QName> headers;
    private final BitSet defaultHeaders, visibleHeaders, searchableHeaders, sortableHeaders, groupableHeaders;
    private final Map<QName, Map<String, String>> headerOptions;
    
    public JournalTypeImpl(Journal journal, NamespacePrefixResolver prefixResolver) {
        this.id = journal.getId();
        this.options = Collections.unmodifiableMap(getOptions(journal.getOption()));
        List<Header> headers = journal.getHeaders().getHeader();
        List<QName> allHeaders = new ArrayList<>(headers.size());
        defaultHeaders = new BitSet(headers.size());
        visibleHeaders = new BitSet(headers.size());
        searchableHeaders = new BitSet(headers.size());
        sortableHeaders = new BitSet(headers.size());
        groupableHeaders = new BitSet(headers.size());
        this.headerOptions = new TreeMap<>();
        int index = 0;
        for(Header header : headers) {
            QName headerKey = QName.createQName(header.getKey(), prefixResolver);
            allHeaders.add(headerKey);
            if(header.isDefault()) defaultHeaders.set(index);
            if(header.isVisible()) visibleHeaders.set(index);
            if(header.isSearchable()) searchableHeaders.set(index);
            if(header.isSortable()) sortableHeaders.set(index);
            if(header.isGroupable()) groupableHeaders.set(index);
            if(header.getOption().size() > 0) {
                this.headerOptions.put(headerKey, 
                        Collections.unmodifiableMap(getOptions(header.getOption())));
            }
            index++;
        }
        this.headers = Collections.unmodifiableList(allHeaders);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public List<QName> getHeaders() {
        return headers;
    }

    @Override
    public List<QName> getDefaultHeaders() {
        return getFeaturedHeaders(defaultHeaders);
    }

    @Override
    public List<QName> getVisibleHeaders() {
        return getFeaturedHeaders(visibleHeaders);
    }

    @Override
    public List<QName> getSearchableHeaders() {
        return getFeaturedHeaders(searchableHeaders);
    }

    @Override
    public List<QName> getSortableHeaders() {
        return getFeaturedHeaders(sortableHeaders);
    }

    @Override
    public List<QName> getGroupableHeaders() {
        return getFeaturedHeaders(groupableHeaders);
    }

    @Override
    public Map<String, String> getHeaderOptions(QName headerKey) {
        Map<String, String> result = headerOptions.get(headerKey);
        return result != null ? result : Collections.<String, String>emptyMap();
    }
    
    @Override
    public boolean isHeaderDefault(QName headerKey) {
        return checkFeature(headerKey, defaultHeaders);
    }

    @Override
    public boolean isHeaderVisible(QName headerKey) {
        return checkFeature(headerKey, visibleHeaders);
    }

    @Override
    public boolean isHeaderSearchable(QName headerKey) {
        return checkFeature(headerKey, searchableHeaders);
    }

    @Override
    public boolean isHeaderSortable(QName headerKey) {
        return checkFeature(headerKey, sortableHeaders);
    }

    @Override
    public boolean isHeaderGroupable(QName headerKey) {
        return checkFeature(headerKey, groupableHeaders);
    }

    private List<QName> getFeaturedHeaders(BitSet featuredHeaders) {
        List<QName> result = new LinkedList<>();
        for (int i = featuredHeaders.nextSetBit(0); i >= 0; i = featuredHeaders.nextSetBit(i+1)) {
            result.add(headers.get(i));
        }
        return Collections.unmodifiableList(result);
    }
    
    private boolean checkFeature(QName headerKey, BitSet featuredHeaders) {
        int index = headers.indexOf(headerKey);
        return index >= 0 ? featuredHeaders.get(index) : false;
    }

    private static Map<String, String> getOptions(List<Option> options) {
        if(options == null) return Collections.emptyMap();
        Map<String, String> optionMap = new HashMap<>(options.size());
        for(Option option : options) {
            optionMap.put(option.getName(), option.getValue().trim());
        }
        return optionMap;
    }

}
