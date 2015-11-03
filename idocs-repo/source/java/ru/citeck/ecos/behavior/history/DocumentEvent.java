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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import ru.citeck.ecos.model.HistoryModel;

public class DocumentEvent implements NodeServicePolicies.OnCreateAssociationPolicy {

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	
	public void init() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
				HistoryModel.TYPE_BASIC_EVENT, HistoryModel.ASSOC_DOCUMENT, 
				new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.EVERY_EVENT));
	}
	
	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		NodeRef event = nodeAssocRef.getSourceRef();
		NodeRef document = nodeAssocRef.getTargetRef();
		if(!nodeService.exists(event) || !nodeService.exists(document)) {
			return;
		}
		
		String documentVersion = (String) nodeService.getProperty(document, ContentModel.PROP_VERSION_LABEL);
		nodeService.setProperty(event, HistoryModel.PROP_DOCUMENT_VERSION, documentVersion);
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}
