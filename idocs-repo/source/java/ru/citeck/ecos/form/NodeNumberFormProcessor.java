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
package ru.citeck.ecos.form;

import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.node.NodeInfo;

public class NodeNumberFormProcessor extends AbstractNumberFormProcessor<NodeRef>
{
	private static Log logger = LogFactory.getLog(NodeNumberFormProcessor.class);
	
	private NodeService nodeService;
	
	@Override
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		super.setServiceRegistry(serviceRegistry);
		this.nodeService = serviceRegistry.getNodeService();
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	@Override
	protected String getItemType(NodeRef item) {
		QName type = this.nodeService.getType(item);
		return type.toPrefixString(this.namespaceService);
	}

	@Override
	protected String getItemURI(NodeRef item) {
		StringBuilder builder = new StringBuilder("/api/node/");
		builder.append(item.getStoreRef().getProtocol()).append("/");
		builder.append(item.getStoreRef().getIdentifier()).append("/");
		builder.append(item.getId());
		return builder.toString();
	}

	@Override
	protected NodeRef getTypedItem(Item item) {
		String itemId = item.getId();
		NodeRef nodeRef = null;
		if(NodeRef.isNodeRef(itemId)) {
			nodeRef = new NodeRef(itemId);
		} else {
			itemId = itemId.replaceFirst("[/]", "://");
			if(NodeRef.isNodeRef(itemId)) {
				nodeRef = new NodeRef(itemId);
			}
		}
		
		if(nodeRef == null) {
			throw new FormNotFoundException(item, new IllegalArgumentException(itemId));
		}
		
		if(!nodeService.exists(nodeRef)) {
            throw new FormNotFoundException(item, 
                    new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef));
		}
		
		return nodeRef;
	}

	@Override
	protected NodeInfo createNodeInfo(NodeRef nodeRef) {
		return nodeInfoFactory.createNodeInfo(nodeRef);
	}

}
