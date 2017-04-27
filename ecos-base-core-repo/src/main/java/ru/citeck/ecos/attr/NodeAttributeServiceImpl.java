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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;

public class NodeAttributeServiceImpl implements NodeAttributeService {
    
    private static final int ATTRIBUTE_PROVIDER_BUCKETS = 5;
    private NodeInfoFactory nodeInfoFactory;
    
    private LinkedList<AttributeProvider> attributeProviders = new LinkedList<>();
    private Map<QName, AttributeProvider> singleAttributeProviders = new HashMap<>();
    private LinkedList<AttributeProvider> patternAttributeProviders = new LinkedList<>();
    private LinkedList<AttributeProvider> defaultAttributeProviders = new LinkedList<>();
    
    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }

    @Override
    public void registerAttributeProvider(AttributeProvider attributeProvider) {
        attributeProviders.add(attributeProvider);
        QNamePattern attributePattern = attributeProvider.getAttributeNamePattern();
        if(attributePattern instanceof QName) {
            singleAttributeProviders.put((QName) attributePattern, attributeProvider);
        } else if(RegexQNamePattern.MATCH_ALL.equals(attributePattern)) {
            defaultAttributeProviders.add(attributeProvider);
        } else {
            patternAttributeProviders.add(attributeProvider);
        }
    }
    
    @Override
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef) {
        return getPersistedAttributeNames(nodeRef, false);
    }

    @Override
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef, boolean justCreated) {
        Set<QName> attributeNames = new HashSet<>(attributeProviders.size() * ATTRIBUTE_PROVIDER_BUCKETS);
        for(AttributeProvider provider : attributeProviders) {
            attributeNames.addAll(provider.getPersistedAttributeNames(nodeRef, justCreated));
        }
        return attributeNames;
    }

    @Override
    public Set<QName> getDefinedAttributeNames(NodeRef nodeRef) {
        Set<QName> attributeNames = new HashSet<>(attributeProviders.size() * ATTRIBUTE_PROVIDER_BUCKETS);
        for(AttributeProvider provider : attributeProviders) {
            attributeNames.addAll(provider.getDefinedAttributeNames(nodeRef));
        }
        return attributeNames;
    }

    @Override
    public Set<QName> getDefinedAttributeNames(QName className) {
        return getDefinedAttributeNames(className, true);
    }

    @Override
    public Set<QName> getDefinedAttributeNames(QName className, boolean inherit) {
        Set<QName> attributeNames = new HashSet<>(attributeProviders.size() * ATTRIBUTE_PROVIDER_BUCKETS);
        for(AttributeProvider provider : attributeProviders) {
            attributeNames.addAll(provider.getDefinedAttributeNames(className, inherit));
        }
        return attributeNames;
    }

    @Override
    public Object getAttribute(NodeRef nodeRef, QName attributeName) {
        AttributeProvider provider = needProvider(attributeName);
        return provider.getAttribute(nodeRef, attributeName);
    }

    @Override
    public Map<QName, Object> getAttributes(NodeRef nodeRef) {
        Map<QName, Object> attributes = new HashMap<>(attributeProviders.size() * ATTRIBUTE_PROVIDER_BUCKETS);
        for(AttributeProvider provider : attributeProviders) {
            Set<QName> attributeNames = provider.getDefaultAttributeNames(nodeRef);
            for(QName attributeName : attributeNames) {
                attributes.put(attributeName, provider.getAttribute(nodeRef, attributeName));
            }
        }
        return attributes;
    }

    @Override
    public void setAttribute(NodeRef nodeRef, QName attributeName, Object value) {
        AttributeProvider provider = needProvider(attributeName);
        provider.setAttribute(nodeRef, attributeName, value);
    }

    @Override
    public void setAttributes(NodeInfo nodeInfo, Map<QName, Object> attributes) {
        for(QName attributeName : attributes.keySet()) {
            AttributeProvider provider = needProvider(attributeName);
            provider.setAttribute(nodeInfo, attributeName, attributes.get(attributeName));
        }
    }

    @Override
    public NodeRef persistAttributes(Map<QName, Object> attributes) {
        NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo(attributes);
        return nodeInfoFactory.persist(nodeInfo, false);
    }

    @Override
    public void setAttributes(NodeRef nodeRef, Map<QName, Object> attributes) {
        NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo();
        nodeInfo.setNodeRef(nodeRef);
        setAttributes(nodeInfo, attributes);
        nodeInfoFactory.persist(nodeInfo, false);
    }

    private AttributeProvider getProvider(QName attributeName) {
        AttributeProvider singleAttributeProvider = singleAttributeProviders.get(attributeName);
        if(singleAttributeProvider != null)
            return singleAttributeProvider;
        for(AttributeProvider provider : patternAttributeProviders) {
            if(provider.getAttributeNamePattern().isMatch(attributeName) && provider.provides(attributeName)) {
                return provider;
            }
        }
        for(AttributeProvider provider : defaultAttributeProviders) {
            if(provider.provides(attributeName)) {
                return provider;
            }
        }
        return null;
    }
    
    private AttributeProvider needProvider(QName attributeName) {
        AttributeProvider provider = getProvider(attributeName);
        if(provider == null) {
            throw new IllegalArgumentException("Attribute " + attributeName + " is not provided");
        }
        return provider;
    }

    @Override
    public QName getAttributeType(QName attributeName) {
        AttributeProvider provider = needProvider(attributeName);
        return provider.getAttributeType(attributeName);
    }

    @Override
    public QName getAttributeSubtype(QName attributeName) {
        AttributeProvider provider = needProvider(attributeName);
        return provider.getAttributeSubtype(attributeName);
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        AttributeProvider provider = needProvider(attributeName);
        return provider.getAttributeValueType(attributeName);
    }

}
