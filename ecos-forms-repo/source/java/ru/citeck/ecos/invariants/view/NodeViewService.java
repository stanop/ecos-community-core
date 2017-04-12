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
package ru.citeck.ecos.invariants.view;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.InputStream;
import java.util.Map;

public interface NodeViewService {

    /**
     * Deploy invariants definition file.
     * 
     * @param definition views definition file
     * @param sourceId id of definition
     */
    void deployDefinition(InputStream definition, String sourceId);

    /**
     * Undeploy invariants definition file.
     * 
     * @param sourceId id of definition
     */
    void undeployDefinition(String sourceId);
    
    /**
     * Checks, if the specified view is registered or not.
     * 
     * @param view node view to check
     * @return true, if the view is registered, false otherwise
     */
    boolean hasNodeView(NodeView view);
    
    /**
     * Get node view for specified class name and default id.
     * 
     * @param className
     * @return
     */
    NodeView getNodeView(QName className);
    
    /**
     * Get node view for specified class name and id.
     * 
     * @param className
     * @param id
     * @return
     */
    NodeView getNodeView(QName className, String id);
    
    /**
     * Get node view for specified nodeRef and default id.
     * 
     * @param nodeRef
     * @return
     */
    NodeView getNodeView(NodeRef nodeRef);
    
    /**
     * Get node view for specified nodeRef and id.
     * 
     * @param nodeRef
     * @param id
     * @return
     */
    NodeView getNodeView(NodeRef nodeRef, String id);
    
    /**
     * Get expanded node view, filled with all necessary elements, according to configuration.
     * 
     * @param view view to expand
     * @return expanded view
     */
    NodeView getNodeView(NodeView view);
    
    /**
     * Save node view to specified node (view with default id).
     * 
     * @param nodeRef node in which to save the node view changes.
     * @param attributes
     */
    void saveNodeView(NodeRef nodeRef, Map<QName, Object> attributes);
    
    /**
     * Save node view to new node (view with default id).
     * 
     * @param typeQName type for which this node view is defined.
     * @param attributes
     */
    NodeRef saveNodeView(QName typeQName, Map<QName, Object> attributes);
    
    /**
     * Save node view to specified node (view with specified id).
     * 
     * @param nodeRef node in which to save the node view changes.
     * @param id view id
     * @param attributes attributes to persist
     * @param params view parameters
     */
    void saveNodeView(NodeRef nodeRef, String id, Map<QName, Object> attributes, Map<String, Object> params);
    
    /**
     * Save node view to new node (view with specified id).
     * 
     * @param typeQName type for which this node view is defined.
     * @param id view id
     * @param attributes attributes to persist
     * @param params view parameters
     */
    NodeRef saveNodeView(QName typeQName, String id, Map<QName, Object> attributes, Map<String, Object> params);
    
}
