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
package ru.citeck.ecos.node;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * Service to manipulate NodeInfo objects.
 * NodeInfo contains information about the node, that may or may not be existent in repository.
 * Using this service one can create NodeInfo from scratch or from some object, 
 *  then fill it using NodeInfo own methods (@see ru.citeck.ecos.node.NodeInfo),
 *  and then persist it back to some node.
 * 
 * @author Sergey Tiunov
 *
 */
public interface NodeInfoFactory {

	/**
	 * Create empty node info object.
	 * @return node info object.
	 */
	public NodeInfo createNodeInfo();

	/**
	 * Create node info object, filled with information about specified node.
	 * @param nodeRef specifies node
	 * @return node info object.
	 */
	public NodeInfo createNodeInfo(NodeRef nodeRef);

	/**
	 * Create node info object, filled with information about specified workflow task.
	 * @param task specifies task
	 * @return node info object.
	 */
	public NodeInfo createNodeInfo(WorkflowTask task);

	/**
	 * Create node info object, filled with information about specified workflow instance.
	 * @param workflow specifies workflow instance
	 * @return node info object.
	 */
	public NodeInfo createNodeInfo(WorkflowInstance workflow);
	
    /**
     * Create node info object, filled with specified attributes.
     * 
     * @param attributes attributes to set
     * 
     * @see NodeAttributeService
     */
    public NodeInfo createNodeInfo(Map<QName, Object> attributes);

    /**
     * Set attributes in specified nodeInfo.
     * 
     * @param nodeInfo node info object to fill
     * @param attributes attributes to set
     * 
     * @see NodeAttributeService
     */
    public void setAttributes(NodeInfo nodeInfo, Map<QName, Object> attributes);

	/**
	 * Updates node with information, specified in nodeInfo.
	 * Does not delete existing properties or associations.
	 * The call should not change nodeInfo.
	 * @param nodeRef node to be updated
	 * @param nodeInfo information to be persisted
	 */
	@Deprecated
	public void persist(NodeRef nodeRef, NodeInfo nodeInfo);

	/**
	 * Updates node with information, specified in nodeInfo.
	 * The call should not change nodeInfo.
	 * @param nodeRef node to be updated
	 * @param nodeInfo information to be persisted
	 * @param full true to delete existing properties and associations
	 */
	@Deprecated
	public void persist(NodeRef nodeRef, NodeInfo nodeInfo, boolean full);

	/**
	 * Creates or updates node with information, specified in nodeInfo.
	 * NodeInfo should contain either parent/parentAssoc/type fields, or nodeRef field.
	 * The call should not change nodeInfo.
	 * @param nodeInfo information to be persisted
	 * @param full true to delete existing properties and associations
	 * @return nodeRef, that was created or updated.
	 */
	public NodeRef persist(NodeInfo nodeInfo, boolean full);

}