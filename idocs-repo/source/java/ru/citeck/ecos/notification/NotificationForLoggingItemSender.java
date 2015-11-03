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
package ru.citeck.ecos.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Notification Sender for documents (ItemType = NodeRef).
 * 
 * The following implementation is used: 
 * - subject line: default
 * - template: retrieved by key = node type
 * - template args: 
 *   {
 *     "item": "nodeRef"
 *   }
 * - recipients: only a document owner receives notification
 * 
 * @author Elena Zaripova
 */
public class NotificationForLoggingItemSender extends AbstractNotificationSender<NodeRef>
{
	public static final String ARG_ITEM = "item";
	public static final String ARG_AUTO_SENT = "autoSent";

	@Override
	public void setServiceRegistry(ServiceRegistry services) {
		super.setServiceRegistry(services);
	}
	
	@Override
	protected NodeRef getNotificationTemplate(NodeRef item) {
		String type = nodeService.getType(item).toPrefixString(namespaceService);
		return getNotificationTemplate(type);
	}

	@Override
	protected Map<String, Serializable> getNotificationArgs(NodeRef item) {
		Map<String, Serializable> args = new HashMap<String, Serializable>();
		args.put(ARG_ITEM, item);
		args.put(ARG_AUTO_SENT, "true");
		return args;
	}
	
	public void sendNotification(NodeRef item)
	{
		super.sendNotification(item);
	}

	protected void sendToAssignee(NodeRef item, Set<String> authorities)
	{
	}

	protected void sendToInitiator(NodeRef item, Set<String> authorities)
	{
	}
	protected void sendToOwner(Set<String> authorities, NodeRef node)
	{
	}

	
	protected void sendToSubscribers(NodeRef item, Set<String> authorities, List<String> taskSubscribers)
	{
	}
}
