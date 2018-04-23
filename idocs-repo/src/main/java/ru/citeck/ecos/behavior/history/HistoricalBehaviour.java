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
package ru.citeck.ecos.behavior.history;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.dictionary.DictionaryService;

import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.history.HistoryService;

public class HistoricalBehaviour implements 
	NodeServicePolicies.OnCreateNodePolicy, 
	VersionServicePolicies.OnCreateVersionPolicy	
{

	private static final Serializable NODE_CREATED = "node.created";
	private static final Serializable NODE_UPDATED = "node.updated";

	private static final String VERSION_UPDATED_COMMENT = "node.version-update.comment";

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private HistoryService historyService;

	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, 
				HistoryModel.ASPECT_HISTORICAL, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
		policyComponent.bindClassBehaviour(VersionServicePolicies.OnCreateVersionPolicy.QNAME, 
				HistoryModel.ASPECT_HISTORICAL, new JavaBehaviour(this, "onCreateVersion", NotificationFrequency.TRANSACTION_COMMIT));
		
	}
	
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef nodeRef = childAssocRef.getChildRef();
		if(!nodeService.exists(nodeRef)) {
			return;
		}
		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
		eventProperties.put(HistoryModel.PROP_NAME, NODE_CREATED);
		eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
		historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
	}

	@Override
	public void onCreateVersion(QName classRef, NodeRef nodeRef,
			Map<String, Serializable> versionProperties, PolicyScope nodeDetails) {
		if(!nodeService.exists(nodeRef)) {
			return;
		}

		Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
		eventProperties.put(HistoryModel.PROP_NAME, NODE_UPDATED);
		eventProperties.put(HistoryModel.ASSOC_DOCUMENT, nodeRef);
		eventProperties.put(HistoryModel.PROP_DOCUMENT_VERSION, nodeDetails.getProperties().get(ContentModel.PROP_VERSION_LABEL));
		String comment = I18NUtil.getMessage(VERSION_UPDATED_COMMENT);
		if (comment != null) {
			eventProperties.put(HistoryModel.PROP_TASK_COMMENT, comment);
		}
		historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, eventProperties);
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}
	
}
