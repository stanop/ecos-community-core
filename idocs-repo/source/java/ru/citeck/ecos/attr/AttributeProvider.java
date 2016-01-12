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

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import ru.citeck.ecos.node.NodeInfo;

public interface AttributeProvider {
    
    /**
     * Attribute name pattern - used for quick provider filtering based on name.
     * 
     * If provider provides only a single attribute, it should return its name.
     * If provider has a single namespace, it should return qname pattern with this namespace.
     * If provider can provide any attribute names, it should return RegexQNamePattern.MATCH_ALL.
     * 
     * The matching of name to this pattern does not necessarily mean, that the attribute is actually provided.
     * To check this, use {@link #provides(QName)} method.
     * 
     * @return pattern for attribute names, that can be provided
     * @see #provides(QName)
     */
    public QNamePattern getAttributeNamePattern();
    
    /**
     * Check whether specified attribute is actually provided by this provider.
     * 
     * @param attributeName
     * @return
     */
    public boolean provides(QName attributeName);
    
    /**
     * Get names of attributes, that are actually persisted in repository for specified node.
     * 
     * @param nodeRef
     * @param justCreated true if node was just created
     * @return
     */
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef, boolean justCreated);
    
    /**
     * Get names of attributes, that are defined in data dictionary for specified node.
     * 
     * @param nodeRef
     * @return
     */
    public Set<QName> getDefinedAttributeNames(NodeRef nodeRef);
    
    /**
     * Get names of attributes, that are defined in data dictionary for specified class.
     * 
     * @param className
     * @return
     */
    public Set<QName> getDefinedAttributeNames(QName className, boolean inherit);
    
    /**
     * Get names of attributes, that are exposed in {@link NodeAttributeService#getAttributes(NodeRef)} method.
     * 
     * @param nodeRef
     * @return
     */
    public Set<QName> getDefaultAttributeNames(NodeRef nodeRef);
    
    /**
     * Get attribute value for the specified node, that is currently persisted in repository.
     * 
     * @param nodeRef
     * @param attributeName
     * @return current attribute value
     */
    public Object getAttribute(NodeRef nodeRef, QName attributeName);
    
    /**
     * Set attribute values for the specified node.
     * 
     * @param nodeRef
     * @param attributes
     */
    public void setAttribute(NodeRef nodeRef, QName attributeName, Object value);
    
    /**
     * Set attribute values for the specified node info.
     * 
     * @param nodeInfo
     * @param attributes
     */
    public void setAttribute(NodeInfo nodeInfo, QName attributeName, Object value);
    
    /**
     * Get type of attribute.
     * 
     * @param attributeName
     * @return
     * @see {@link NodeAttributeService#getAttributeType(QName)}
     */
    public QName getAttributeType(QName attributeName);
    
    /**
     * Get sub-type of attribute.
     * 
     * @param attributeName
     * @return
     * @see {@link NodeAttributeService#getAttributeSubtype(QName)}
     */
    public QName getAttributeSubtype(QName attributeName);
    
    /**
     * Get the type of value, required by attribute.
     * If the value is multiple, return the type of a single item.
     * 
     * @param attributeName
     * @return
     */
    public Class<?> getAttributeValueType(QName attributeName);

}
