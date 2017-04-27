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
package ru.citeck.ecos.counter;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import ru.citeck.ecos.node.NodeInfo;

public interface EnumerationService 
{
    
    public static final String KEY_COUNT = "count";
    public static final String KEY_NODE = "node";
    public static final String KEY_TEMPLATE = "template";
    
    /**
     * Get template by name.
     * 
     * @param templateName
     * @return template nodeRef or null, if template is not found.
     */
    public NodeRef getTemplate(String templateName);

    /**
     * Checks if the specified node is a valid enumeration template.
     * 
     * @param nodeRef
     * @return
     */
    public boolean isTemplate(NodeRef nodeRef);
    
    /**
     * Generate number for the node by template with incrementing the counter.
     * 
     * @param template - template reference
     * @param nodeInfo - node information
     * @return - generated number
     * @throws EnumerationException - thrown if number can not be generated
     */
    public String getNumber(NodeRef template, NodeInfo nodeInfo) throws EnumerationException;

    /**
     * Generate number for the node by template.
     * 
     * If count parameter is not null, the counter is not incremented.
     * 
     * @param template - template reference
     * @param nodeInfo - node information
     * @param count - count value 
     * @return - generated number
     * @throws EnumerationException - thrown if number can not be generated
     */
    public String getNumber(NodeRef template, NodeInfo nodeInfo, String count) throws EnumerationException;

    /**
     * Generate number for the node by template with incrementing the counter.
     * 
     * @param template - template reference
     * @param nodeRef - node reference
     * @return - generated number
     * @throws EnumerationException - thrown if number can not be generated
     */
    public String getNumber(NodeRef template, NodeRef nodeRef) throws EnumerationException;

    /**
     * Generate number for the node by template.
     * 
     * If count parameter is not null, the counter is not incremented.
     * 
     * @param template - template reference
     * @param nodeRef - node reference
     * @param count - count value
     * @return - generated number
     * @throws EnumerationException - thrown if number can not be generated
     */
    public String getNumber(NodeRef template, NodeRef nodeRef, String count) throws EnumerationException;

    /**
     * Generate number from the model.
     * 
     * This is the most general version of getNumber method, all other methods are added for convenience.
     * The model can contain 'node' entry (see KEY_NODE constant), that is automatically converted to Node.
     * The model can also contain 'count' entry (see KEY_COUNT contant), 
     *   and if does not contain it, count is generated from template.
     * 
     * @param template freemarker template string
     * @param model model for template
     * @return generated number
     * @throws EnumerationException
     */
    public String getNumber(NodeRef template, Map<String, Object> model) throws EnumerationException;
    
}
