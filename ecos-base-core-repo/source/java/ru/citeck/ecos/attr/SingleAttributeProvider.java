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

import java.util.Collections;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.utils.ReflectionUtils;

public abstract class SingleAttributeProvider<T> extends AbstractAttributeProvider {

    private final QName attributeName;
    private final QName attributeType;
    private final QName attributeSubtype;
    private final Class<T> type;
    
    public SingleAttributeProvider(QName attributeName) {
        this(attributeName, AttributeModel.TYPE_VIRTUAL, AttributeModel.TYPE_VIRTUAL);
    }
    
    public SingleAttributeProvider(QName attributeName, QName attributeType, QName attributeSubtype) {
        if(attributeName == null) {
            throw new IllegalArgumentException("Attribute name should be specified");
        }
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.attributeSubtype = attributeSubtype;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) ReflectionUtils.getGenericParameterClass(this, 0);
        this.type = type;
    }
    
    @Override
    public QNamePattern getAttributeNamePattern() {
        return attributeName;
    }

    @Override
    public boolean provides(QName attributeName) {
        return this.attributeName.equals(attributeName);
    }

    @Override
    public QName getAttributeType(QName attributeName) {
        return attributeType;
    }

    @Override
    public QName getAttributeSubtype(QName attributeName) {
        return attributeSubtype;
    }

    private Set<QName> getAttributeNameSet(boolean includeAttributeName) {
        return includeAttributeName ? Collections.singleton(attributeName) : Collections.<QName>emptySet();
    }

    @Override
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef, boolean justCreated) {
        return getAttributeNameSet(isDefined(nodeRef));
    }

    @Override
    public Set<QName> getDefinedAttributeNames(NodeRef nodeRef) {
        return getAttributeNameSet(isDefined(nodeRef));
    }
    
    @Override
    public Set<QName> getDefinedAttributeNames(QName typeName, boolean inherit) {
        return getAttributeNameSet(isDefined(typeName, inherit));
    }

    @Override
    public Set<QName> getDefaultAttributeNames(NodeRef nodeRef) {
        return getAttributeNameSet(isDefault() && isDefined(nodeRef));
    }

    @Override
    public Object getAttribute(NodeRef nodeRef, QName attributeName) {
        return getValue(nodeRef);
    }

    @Override
    public void setAttribute(NodeRef nodeRef, QName attributeName, Object value) {
        setValue(nodeRef, convertValue(value, type));
    }

    @Override
    public void setAttribute(NodeInfo node, QName attributeName, Object value) {
        setValue(node, convertValue(value, type));
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        return type;
    }

    protected boolean isDefined(NodeRef nodeRef) {
        return true;
    }

    protected boolean isDefined(QName typeName, boolean inherit) {
        return inherit;
    }

    protected boolean isDefault() {
        return true;
    }

    protected abstract T getValue(NodeRef nodeRef);

    protected abstract void setValue(NodeRef nodeRef, T value);
    
    protected abstract void setValue(NodeInfo nodeInfo, T value);
    
}
