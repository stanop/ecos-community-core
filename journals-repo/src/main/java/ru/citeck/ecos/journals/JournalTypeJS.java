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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;

import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

public class JournalTypeJS {
    
    private JournalType impl;
    private NamespaceService namespaceService;
    
    JournalTypeJS(JournalType impl, ServiceRegistry serviceRegistry) {
        this.impl = impl;
        this.namespaceService = serviceRegistry.getNamespaceService();
    }
    
    public String getId() {
        return impl.getId();
    }
    
    public Map<String, String> getOptions() {
        return impl.getOptions();
    }
    
    public String[] getAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getAttributes(), namespaceService);
    }
    
    public String[] getDefaultAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getDefaultAttributes(), namespaceService);
    }
    
    public String[] getVisibleAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getVisibleAttributes(), namespaceService);
    }
    
    public String[] getSearchableAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getSearchableAttributes(), namespaceService);
    }
    
    public String[] getSortableAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getSortableAttributes(), namespaceService);
    }
    
    public String[] getGroupableAttributes() {
        return JavaScriptImplUtils.convertQNames(impl.getGroupableAttributes(), namespaceService);
    }
    
    public Map<String, String> getAttributeOptions(String attributeKey) {
        return impl.getAttributeOptions(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }

    public boolean isAttributeDefault(String attributeKey) {
        return impl.isAttributeDefault(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }
    
    public boolean isAttributeVisible(String attributeKey) {
        return impl.isAttributeVisible(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }
    
    public boolean isAttributeSearchable(String attributeKey) {
        return impl.isAttributeSearchable(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }
    
    public boolean isAttributeSortable(String attributeKey) {
        return impl.isAttributeSortable(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }
    
    public boolean isAttributeGroupable(String attributeKey) {
        return impl.isAttributeGroupable(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }

    public List<JournalBatchEdit> getBatchEdit(String attributeKey) {
        return impl.getBatchEdit(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }

    public List<JournalGroupAction> getGroupActions() {
        return impl.getGroupActions();
    }

    public JournalCriterion getCriterion(String attributeKey) {
        return impl.getCriterion(JavaScriptImplUtils.convertQName(attributeKey, namespaceService));
    }

    public String getFieldsSchema() {
        return impl.getFieldsSchema();
    }
    
}
