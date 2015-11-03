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
package ru.citeck.ecos.attr.prov;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.attr.AbstractAttributeProvider;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.utils.ConvertUtils;
import ru.citeck.ecos.utils.DictionaryUtils;

public class PropertyAttributes extends AbstractAttributeProvider {

    @Override
    public QNamePattern getAttributeNamePattern() {
        return RegexQNamePattern.MATCH_ALL;
    }

    @Override
    public boolean provides(QName attributeName) {
        return getDefinition(attributeName) != null;
    }

    @Override
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef, boolean justCreated) {
        return nodeService.getProperties(nodeRef).keySet();
    }

    @Override
    public Set<QName> getDefaultAttributeNames(NodeRef nodeRef) {
        // all persisted properties are shown by default
        return getPersistedAttributeNames(nodeRef, false);
    }

    @Override
    public Set<QName> getDefinedAttributeNames(NodeRef nodeRef) {
        return DictionaryUtils.getAllPropertyNames(nodeRef, nodeService, dictionaryService);
    }

    @Override
    public Set<QName> getDefinedAttributeNames(QName typeName) {
        return DictionaryUtils.getAllPropertyNames(Collections.singleton(typeName), dictionaryService);
    }

    @Override
    public Object getAttribute(NodeRef nodeRef, QName attributeName) {
        return nodeService.getProperty(nodeRef, attributeName);
    }

    @Override
    public void setAttribute(NodeInfo nodeInfo, QName attributeName, Object value) {
        PropertyDefinition propDef = needDefinition(attributeName);
        if(DataTypeDefinition.CONTENT.equals(propDef.getDataType().getName())) {
            // do not convert content properties
            // it is now done by NodeInfo
        } else {
            value = ConvertUtils.convertValue(value, getValueClass(propDef), propDef.isMultiValued());
        }
        nodeInfo.setProperty(attributeName, (Serializable) value);
    }

    @Override
    public QName getAttributeType(QName attributeName) {
        return AttributeModel.TYPE_PROPERTY;
    }

    @Override
    public QName getAttributeSubtype(QName attributeName) {
        return needDefinition(attributeName).getDataType().getName();
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        PropertyDefinition propDef = needDefinition(attributeName);
        return getValueClass(propDef);
    }

    private Class<?> getValueClass(PropertyDefinition propDef) {
        String className = propDef.getDataType().getJavaClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not find property datatype class: " + className);
        }
    }

    private PropertyDefinition getDefinition(QName attributeName) {
        return dictionaryService.getProperty(attributeName);
    }

    private PropertyDefinition needDefinition(QName attributeName) {
        PropertyDefinition propDef = getDefinition(attributeName);
        if(propDef == null) 
            throw new IllegalArgumentException("Property " + attributeName + " does not exist");
        return propDef;
    }

}
