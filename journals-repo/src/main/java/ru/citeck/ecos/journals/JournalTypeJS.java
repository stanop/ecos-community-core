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

import java.util.List;
import java.util.Map;

public class JournalTypeJS {
    
    private JournalType impl;
    
    JournalTypeJS(JournalType impl) {
        this.impl = impl;
    }
    
    public String getId() {
        return impl.getId();
    }
    
    public Map<String, String> getOptions() {
        return impl.getOptions();
    }
    
    public String[] getAttributes() {
        List<String> attributes = impl.getAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public String[] getDefaultAttributes() {
        List<String> attributes = impl.getDefaultAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public String[] getVisibleAttributes() {
        List<String> attributes = impl.getVisibleAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public String[] getSearchableAttributes() {
        List<String> attributes = impl.getSearchableAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public String[] getSortableAttributes() {
        List<String> attributes = impl.getSortableAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public String[] getGroupableAttributes() {
        List<String> attributes = impl.getGroupableAttributes();
        return attributes.toArray(new String[0]);
    }
    
    public Map<String, String> getAttributeOptions(String attributeKey) {
        return impl.getAttributeOptions(attributeKey);
    }

    public boolean isAttributeDefault(String attributeKey) {
        return impl.isAttributeDefault(attributeKey);
    }
    
    public boolean isAttributeVisible(String attributeKey) {
        return impl.isAttributeVisible(attributeKey);
    }
    
    public boolean isAttributeSearchable(String attributeKey) {
        return impl.isAttributeSearchable(attributeKey);
    }
    
    public boolean isAttributeSortable(String attributeKey) {
        return impl.isAttributeSortable(attributeKey);
    }
    
    public boolean isAttributeGroupable(String attributeKey) {
        return impl.isAttributeGroupable(attributeKey);
    }

    public List<JournalBatchEdit> getBatchEdit(String attributeKey) {
        return impl.getBatchEdit(attributeKey);
    }

    public JournalFormatter getFormatter(String attributeKey) {
        return impl.getFormatter(attributeKey);
    }

    public List<JournalGroupAction> getGroupActions() {
        return impl.getGroupActions();
    }

    public JournalCriterion getCriterion(String attributeKey) {
        return impl.getCriterion(attributeKey);
    }

    public String getDataSource() {
        return impl.getDataSource();
    }
}
