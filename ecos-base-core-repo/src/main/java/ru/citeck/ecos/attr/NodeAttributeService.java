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

import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.node.NodeInfo;

/**
 * Node Attribute Service implements simplified access to repository nodes 
 * by introducing the concept of attribute.
 * The concept of attribute is simple: any node is the attribute-value map 
 * and nothing more.
 * 
 * Access to attributes is encapsulated in pluggable AttributeProvider implementations.
 * For example, the following attributes can be used:
 * <ul>
 *     <li>properties</li>
 *     <li>target associations</li>
 *     <li>special attributes: type, aspects, parent, etc.</li>
 *     <li>anything else, for which attribute provider is defined</li>
 * </ul>
 * 
 * Attribute names should not be ambiguous.
 * For this requirement to be met, the following rule is used:
 * properties, target associations and child associations use the names, that are defined in data dictionary;
 * all other attributes use unique namespaces for their names, e.g. special attributes use "attr" namespace.
 * 
 * @author Sergey Tiunov
 *
 */
// TODO add Auditable annotations
public interface NodeAttributeService {
    
    /**
     * Register attribute provider in service.
     * 
     * @param attributeProvider attribute provider to register
     */
    public void registerAttributeProvider(AttributeProvider attributeProvider);
    
    /**
     * Get names of attributes, that are persisted for the given node in repository.
     * 
     * @param nodeRef
     * @return
     */
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef);
    
    /**
     * Get names of attributes, that are persisted for the given node in repository.
     * 
     * @param nodeRef
     * @param justCreated - if node was just created
     * @return
     */
    public Set<QName> getPersistedAttributeNames(NodeRef nodeRef, boolean justCreated);
    
    /**
     * Get names of attributes, that are defined for the given node in data dictionary.
     * 
     * @param nodeRef
     * @return
     */
    public Set<QName> getDefinedAttributeNames(NodeRef nodeRef);
    
    /**
     * Get names of attributes, that are defined for the given class in data dictionary (including inherited attributes).
     * 
     * @param className name of type/aspect for which to get defined attributes
     * @return list of defined attributes
     */
    public Set<QName> getDefinedAttributeNames(QName className);
    
    /**
     * Get names of attributes, that are defined for the given class in data dictionary.
     * 
     * @param className name of type/aspect for which to get defined attributes
     * @param inherit inherit attributes from parent type and default aspects or not
     * @return list of defined attributes
     */
    public Set<QName> getDefinedAttributeNames(QName className, boolean inherit);
    
    /**
     * Get attribute value for the specified node, that is currently persisted in repository.
     * 
     * @param nodeRef
     * @param attributeName
     * @return current attribute value
     */
    public Object getAttribute(NodeRef nodeRef, QName attributeName);
    
    /**
     * Get persisted attribute values for the specified node.
     * 
     * Some attributes (e.g. child-associations) may have quite massive values and are not included in this list.
     * Their values can be retrieved separately via getAttributeValue method.
     * To get names of all attributes, defined for the given node, use getDefinedAttributeNames method.
     * 
     * @param nodeRef reference of node, which attributes are requested
     * @return map of attribute values
     */
    public Map<QName, Object> getAttributes(NodeRef nodeRef);
    
    /**
     * Set attribute value for the specified node.
     * 
     * @param nodeRef
     * @param attributeName
     * @param value
     */
    public void setAttribute(NodeRef nodeRef, QName attributeName, Object value);
    
    /**
     * Set attribute values for the specified node.
     * 
     * @param nodeRef
     * @param attributes
     */
    public void setAttributes(NodeRef nodeRef, Map<QName, Object> attributes);
    
    /**
     * Set attribute values for the specified node info.
     * 
     * @param nodeInfo
     * @param attributes
     */
    public void setAttributes(NodeInfo nodeInfo, Map<QName, Object> attributes);
    
    /**
     * Persist specified attributes in repository.
     * 
     * If attr:noderef attribute is specified, the node referenced by this noderef is updated. 
     * Otherwise, the attributes should contain attr:types, attr:parent and attr:parentassoc 
     *   values, to be able to create new node.
     * 
     * @param attributes
     * @return created or updated node reference
     */
    public NodeRef persistAttributes(Map<QName, Object> attributes);
    
    /**
     * Get type of attribute.
     * 
     * Generally, every attribute provider usually provides a single attribute type.
     * 
     * @param attributeName
     * @return
     */
    public QName getAttributeType(QName attributeName);
    
    /**
     * Get sub-type of attribute.
     * 
     * Attribute sub-types are scoped within attribute types.
     * E.g. type is "attr:property", and subtype is "d:text" (text property).
     * 
     * @param attributeName
     * @return
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
