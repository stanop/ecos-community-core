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
package ru.citeck.ecos.attr;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;

public abstract class AbstractAttributeProvider implements AttributeProvider {

    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected NodeInfoFactory nodeInfoFactory;
    private NodeAttributeService nodeAttributeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }

    public void setNodeAttributeService(NodeAttributeService nodeAttributeService) {
        this.nodeAttributeService = nodeAttributeService;
    }
    
    public void init() {
        nodeAttributeService.registerAttributeProvider(this);
    }
    
    @Override
    public void setAttribute(NodeRef nodeRef, QName attributeName, Object value) {
        NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo();
        nodeInfo.setNodeRef(nodeRef);
        setAttribute(nodeInfo, attributeName, value);
        nodeInfoFactory.persist(nodeInfo, false);
    }

    protected QName convertToQName(Object name) {
        if(name instanceof QName) return (QName) name;
        if(name instanceof String) return QName.createQName((String) name, namespaceService);
        throw new IllegalArgumentException("Can not convert to QName from " + name);
    }
    
    protected List<QName> convertToQNameList(List<?> names) {
        List<QName> qnames = new ArrayList<>(names.size());
        for(Object name : names) { 
            qnames.add(convertToQName(name));
        }
        return qnames;
    }
    
    // TODO refactor: delegate conversions to ConvertUtils
    
    @SuppressWarnings("unchecked")
    protected <T> T convertValue(Object value, Class<T> type) {
        if(value != null && !type.isInstance(value)) {
            if(type == QName.class && value instanceof String) {
                return (T) QName.createQName((String) value, namespaceService);
            }
            try {
                Constructor<?> constructor = type.getConstructor(value.getClass());
                value = constructor.newInstance(value);
            } catch (Exception e) {
                throw new IllegalArgumentException("Value type is not expected: " + value.getClass() + " vs " + type, e);
            }
        }
        return (T) value;
    }

    protected <T> List<T> asList(Collection<? extends T> items) {
        return items == null || items.isEmpty() 
                ? Collections.<T>emptyList() 
                : new ArrayList<>(items);
    }
}
