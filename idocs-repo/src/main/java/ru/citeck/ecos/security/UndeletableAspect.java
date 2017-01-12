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
package ru.citeck.ecos.security;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Undeletable aspect behaviour bean.
 * Unlike org.alfresco.repo.node.UndeletableAspect this behaviour allows deletion of working copies.
 * 
 * @author Sergey Tiunov
 */
public class UndeletableAspect implements NodeServicePolicies.BeforeDeleteNodePolicy
{
   private PolicyComponent policyComponent;
   private NodeService nodeService;
   
   /**
    * Set the policy component
    * 
    * @param policyComponent   policy component
    */
   public void setPolicyComponent(PolicyComponent policyComponent)
   {
       this.policyComponent = policyComponent;
   }
   
   /**
    * Set the node service
    * 
    * @param nodeService   node service
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }
   
   /**
    * Initialise method
    */
   public void init()
   {
       this.policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
               ContentModel.ASPECT_UNDELETABLE,
               new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));
   }

   /**
    * Ensures that undeletable nodes cannot be deleted by default.
    */
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
    	// process only existing nodes
    	if(!nodeService.exists(nodeRef)) {
    		return;
    	}
    	// do not process working copies
    	if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
    		return;
    	}
    	
        QName nodeType = nodeService.getType(nodeRef);
        throw new AlfrescoRuntimeException(nodeType.toPrefixString() + " deletion is not allowed. Attempted to delete " + nodeRef);
    }
}
