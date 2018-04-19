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
package ru.citeck.ecos.behavior.idocs;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

import ru.citeck.ecos.model.PassportModel;

public class PassportBehaviour implements NodeServicePolicies.OnCreateNodePolicy
{
	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private PermissionService permissionService;
	
	public void init() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, 
				PassportModel.TYPE_PASSPORT, 
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef passport = childAssocRef.getChildRef();
		if(!nodeService.exists(passport)) {
			return;
		}
		
		NodeRef passportOwner = getPassportOwner(passport);
		if(passportOwner == null || !nodeService.exists(passportOwner)) {
			return;
		}
		
		String passportOwnerName = (String) nodeService.getProperty(passportOwner, ContentModel.PROP_USERNAME);
		if(passportOwnerName == null) {
			return;
		}
		
		permissionService.setPermission(passport, passportOwnerName, PermissionService.EDITOR, true);
	}

	private NodeRef getPassportOwner(NodeRef passport) {
		List<AssociationRef> owners = nodeService.getTargetAssocs(passport, PassportModel.ASSOC_PERSON);
		if(owners.size() > 0) {
			return owners.get(0).getTargetRef();
		} else {
			return null;
		}
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

}

