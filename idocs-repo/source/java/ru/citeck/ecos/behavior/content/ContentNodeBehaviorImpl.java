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
package ru.citeck.ecos.behavior.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

public class ContentNodeBehaviorImpl implements
		NodeServicePolicies.OnUpdateNodePolicy,
		ContentNodeBehavior {

	private static final String ERROR_EXCEEDED_CONTENT_SIZE = "error.exceeded.content.size";

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private Map<QName, Long> sizeLimits = new HashMap<QName, Long>();

	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		if (!nodeService.exists(nodeRef))
			return;

		checkSize(nodeRef);
	}

	@Override
	public void registerSizeLimits(Map<QName, Long> sizeLimits) {
		Set<QName> addedKeys = new HashSet<QName>(sizeLimits.keySet());
		addedKeys.removeAll(this.sizeLimits.keySet());
		for (QName type : addedKeys) {
			policyComponent.bindClassBehaviour(
					NodeServicePolicies.OnUpdateNodePolicy.QNAME,
					type,
					new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
		}
		this.sizeLimits.putAll(sizeLimits);
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	protected void checkSize(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		Long sizeLimit = sizeLimits.get(type);
		if (sizeLimit == null || sizeLimit.longValue() == 0L)
			return;

		Serializable contentObj = nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
		if (contentObj == null || !(contentObj instanceof ContentData))
			return;

		ContentData content = (ContentData)contentObj;
		long contentSize = content.getSize();
		if (contentSize > sizeLimit) {
			String message = I18NUtil.getMessage(ERROR_EXCEEDED_CONTENT_SIZE, contentSize, sizeLimit);
			if (message == null)
				message = ERROR_EXCEEDED_CONTENT_SIZE;
			throw new AlfrescoRuntimeException("\n" + message);
		}
	}

}
