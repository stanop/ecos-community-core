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
package ru.citeck.ecos.behavior.common;

import java.util.List;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.ServiceRegistry;

import ru.citeck.ecos.notification.NotificationForLoggingItemSender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

import ru.citeck.ecos.model.NotificationLoggingModel;

public class SendNotificationForLoggingItemBehaviour implements 
		NodeServicePolicies.OnCreateNodePolicy {
	// common properties
	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	protected ServiceRegistry services;
    protected NotificationForLoggingItemSender sender;

	// distinct properties
	protected QName className;
    private static final Log logger = LogFactory.getLog(SendNotificationForLoggingItemBehaviour.class);
	protected boolean enabled;
	private List<String> eventTypes;

	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, className, 
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

	}
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.services = serviceRegistry;
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) 
	{
		NodeRef nodeRef = childAssocRef.getChildRef();
		if(nodeService.exists(nodeRef) && enabled)
		{
			logger.debug("!!!object created. need to send mail");
			String eventType = (String)nodeService.getProperty(nodeRef, NotificationLoggingModel.PROP_EVENT_TYPE);
			logger.debug("!!!eventType "+eventType);
			if(eventTypes!=null && eventTypes.contains(eventType))
			{
				sender.sendNotification(nodeRef);
				logger.debug("!!!SEND");
			}
		}
		
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setClassName(QName className) {
		this.className = className;
	}

	/**
	 * Set NotificationSender.
	 * @param sender
	 */
	public void setSender(NotificationForLoggingItemSender sender) {
		this.sender = sender;
	}

	/**
	* enabled
	* @param true or false
	*/
	public void setEnabled(Boolean enabled) {
    	this.enabled = enabled.booleanValue();
    }
	
	public void setEventTypes(List<String> eventTypes) {
    	this.eventTypes = eventTypes;
    }
	
}
