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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.ServiceRegistry;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.RegexQNamePattern;

public class CountChildAssociations implements NodeServicePolicies.OnCreateChildAssociationPolicy {
	// common properties
	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	protected ServiceRegistry services;
    protected QName propertyForCountChild;
    protected QName childAssocType;

	// distinct properties
	protected QName className;

	public void init() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, className,
				new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT)
		);
	}
	

	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNew) {
        if(childAssocType!=null && childAssocType.equals(childAssociationRef.getTypeQName()))
        {
            NodeRef parent = childAssociationRef.getParentRef();
            if(nodeService.exists(parent))
            {
                List<ChildAssociationRef> existingAssocs = nodeService.getChildAssocs(parent, childAssocType, RegexQNamePattern.MATCH_ALL);
                if(propertyForCountChild!=null)
                {
                    nodeService.setProperty(parent, propertyForCountChild, existingAssocs.size());
                }
            }
        }
		
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.services = serviceRegistry;
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

	public void setPropertyForCountChild(QName propertyForCountChild) {
		this.propertyForCountChild = propertyForCountChild;
	}

	public void setChildAssocType(QName childAssocType) {
		this.childAssocType = childAssocType;
	}
}
