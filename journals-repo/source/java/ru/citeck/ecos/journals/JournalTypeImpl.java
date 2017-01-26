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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.journals.xml.BatchEdit;
import ru.citeck.ecos.journals.xml.Header;
import ru.citeck.ecos.journals.xml.Journal;
import ru.citeck.ecos.journals.xml.Option;

class JournalTypeImpl implements JournalType {

    private final String id;
    private final Map<String, String> options;
    private final List<QName> attributes;
    private final BitSet defaultAttributes, visibleAttributes, searchableAttributes, sortableAttributes, groupableAttributes;
    private final Map<QName, Map<String, String>> attributeOptions;
    private final Map<QName, List<JournalBatchEdit>> batchEdit;
    
    public JournalTypeImpl(Journal journal, NamespacePrefixResolver prefixResolver, ServiceRegistry serviceRegistry) {

        this.id = journal.getId();
        this.options = Collections.unmodifiableMap(getOptions(journal.getOption()));
        List<Header> headers = journal.getHeaders().getHeader();
        List<QName> allAttributes = new ArrayList<>(headers.size());
        batchEdit = new HashMap<>();
        defaultAttributes = new BitSet(allAttributes.size());
        visibleAttributes = new BitSet(allAttributes.size());
        searchableAttributes = new BitSet(allAttributes.size());
        sortableAttributes = new BitSet(allAttributes.size());
        groupableAttributes = new BitSet(allAttributes.size());
        this.attributeOptions = new TreeMap<>();

        int index = 0;
        for(Header header : headers) {
            QName attributeKey = QName.createQName(header.getKey(), prefixResolver);
            allAttributes.add(attributeKey);
            if(header.isDefault()) defaultAttributes.set(index);
            if(header.isVisible()) visibleAttributes.set(index);
            if(header.isSearchable()) searchableAttributes.set(index);
            if(header.isSortable()) sortableAttributes.set(index);
            if(header.isGroupable()) groupableAttributes.set(index);
            if(header.getOption().size() > 0) {
                this.attributeOptions.put(attributeKey,
                        Collections.unmodifiableMap(getOptions(header.getOption())));
            }
            List<JournalBatchEdit> attributeBatchEdit = new ArrayList<>();
            for (BatchEdit batchEdit : header.getBatchEdit()) {
                attributeBatchEdit.add(new JournalBatchEdit(batchEdit, journal.getId(),
                                                            attributeKey, prefixResolver,
                                                            serviceRegistry));
            }
            batchEdit.put(attributeKey, attributeBatchEdit);
            index++;
        }
        this.attributes = Collections.unmodifiableList(allAttributes);
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
    public List<QName> getAttributes() {
        return attributes;
    }

    @Override
    public List<QName> getDefaultAttributes() {
        return getFeaturedAttributes(defaultAttributes);
    }

    @Override
    public List<QName> getVisibleAttributes() {
        return getFeaturedAttributes(visibleAttributes);
    }

    @Override
    public List<QName> getSearchableAttributes() {
        return getFeaturedAttributes(searchableAttributes);
    }

    @Override
    public List<QName> getSortableAttributes() {
        return getFeaturedAttributes(sortableAttributes);
    }

    @Override
    public List<QName> getGroupableAttributes() {
        return getFeaturedAttributes(groupableAttributes);
    }

    @Override
    public Map<String, String> getAttributeOptions(QName attributeKey) {
        Map<String, String> result = attributeOptions.get(attributeKey);
        return result != null ? result : Collections.<String, String>emptyMap();
    }
    
    @Override
    public boolean isAttributeDefault(QName attributeKey) {
        return checkFeature(attributeKey, defaultAttributes);
    }

    @Override
    public boolean isAttributeVisible(QName attributeKey) {
        return checkFeature(attributeKey, visibleAttributes);
    }

    @Override
    public boolean isAttributeSearchable(QName attributeKey) {
        return checkFeature(attributeKey, searchableAttributes);
    }

    @Override
    public boolean isAttributeSortable(QName attributeKey) {
        return checkFeature(attributeKey, sortableAttributes);
    }

    @Override
    public boolean isAttributeGroupable(QName attributeKey) {
        return checkFeature(attributeKey, groupableAttributes);
    }

    @Override
    public List<JournalBatchEdit> getBatchEdit(QName attributeKey) {
        List<JournalBatchEdit> batchEdit = this.batchEdit.get(attributeKey);
        List<JournalBatchEdit> result = new ArrayList<>(batchEdit.size());
        for (JournalBatchEdit edit : batchEdit) {
            if (edit.getEvaluator().evaluate()) {
                result.add(edit);
            }
        }
        return result;
    }

    private List<QName> getFeaturedAttributes(BitSet featuredAttributes) {
        List<QName> result = new LinkedList<>();
        for (int i = featuredAttributes.nextSetBit(0); i >= 0; i = featuredAttributes.nextSetBit(i+1)) {
            result.add(attributes.get(i));
        }
        return Collections.unmodifiableList(result);
    }
    
    private boolean checkFeature(QName attributeKey, BitSet featuredAttributes) {
        int index = attributes.indexOf(attributeKey);
        return index >= 0 ? featuredAttributes.get(index) : false;
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
