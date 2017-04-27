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

import java.util.ArrayList;
import java.util.Collection;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * AssociationWalker helper class.
 * Allows one to walk throw associations as specified by parameters.
 * 
 * @author Sergey Tiunov
 */
class AssociationWalker 
{
	private NodeService nodeService;

	private boolean propagateTargetAssociations;
	private boolean propagatePrimaryChildAssociations;
	private boolean propagateSecondaryChildAssociations;

	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	public boolean getPropagateTargetAssociations() {
		return propagateTargetAssociations;
	}
	public void setPropagateTargetAssociations(
			boolean propagateTargetAssociations) {
		this.propagateTargetAssociations = propagateTargetAssociations;
	}
	public boolean getPropagatePrimaryChildAssociations() {
		return propagatePrimaryChildAssociations;
	}
	public void setPropagatePrimaryChildAssociations(
			boolean propagatePrimaryChildAssociations) {
		this.propagatePrimaryChildAssociations = propagatePrimaryChildAssociations;
	}
	public boolean getPropagateSecondaryChildAssociations() {
		return propagateSecondaryChildAssociations;
	}
	public void setPropagateSecondaryChildAssociations(
			boolean propagateSecondaryChildAssociations) {
		this.propagateSecondaryChildAssociations = propagateSecondaryChildAssociations;
	}
	
	/////////////////////////////////////////////////////////////////
	//                ASSOCIATION WALKER INTERFACE                 //
	/////////////////////////////////////////////////////////////////

	public Collection<NodeRef> getForwardAssocs(NodeRef nodeRef) {
		Collection<NodeRef> nodeRefs = new ArrayList<NodeRef>();
		addTargetAssocs(nodeRefs, nodeRef);
		addPrimaryChildAssocs(nodeRefs, nodeRef);
		addSecondaryChildAssocs(nodeRefs, nodeRef);
		return nodeRefs;
	}

	public Collection<NodeRef> getPrimaryAssocs(NodeRef nodeRef) {
		Collection<NodeRef> nodeRefs = new ArrayList<NodeRef>();
		this.addPrimaryChildAssocs(nodeRefs, nodeRef);
		return nodeRefs;
	}
	
	public Collection<NodeRef> getSecondaryAssocs(NodeRef nodeRef) {
		Collection<NodeRef> nodeRefs = new ArrayList<NodeRef>();
		addSecondaryChildAssocs(nodeRefs, nodeRef);
		addTargetAssocs(nodeRefs, nodeRef);
		return nodeRefs;
	}
	
	private void addTargetAssocs(Collection<NodeRef> nodeRefs, NodeRef nodeRef) {
		if(propagateTargetAssociations) {
			Collection<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
			for(AssociationRef assoc : assocs) {
				nodeRefs.add(assoc.getTargetRef());
			}
		}
	}
	
	private void addPrimaryChildAssocs(Collection<NodeRef> nodeRefs, NodeRef nodeRef) {
		if(propagatePrimaryChildAssociations) {
			Collection<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef);
			for(ChildAssociationRef assoc : assocs) {
				if(assoc.isPrimary())
				{
					nodeRefs.add(assoc.getChildRef());
				}
			}
		}
	}
	
	private void addSecondaryChildAssocs(Collection<NodeRef> nodeRefs, NodeRef nodeRef) {
		if(propagateSecondaryChildAssociations) {
			Collection<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef);
			for(ChildAssociationRef assoc : assocs) {
				if(!assoc.isPrimary())
				{
					nodeRefs.add(assoc.getChildRef());
				}
			}
		}
	}
	
	public Collection<NodeRef> getBackwardAssocs(NodeRef nodeRef) {
		Collection<NodeRef> nodeRefs = new ArrayList<NodeRef>();
		if(propagateTargetAssociations) {
			Collection<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
			for(AssociationRef assoc : assocs) {
				nodeRefs.add(assoc.getTargetRef());
			}
		}
		if(propagatePrimaryChildAssociations || propagateSecondaryChildAssociations) {
			Collection<ChildAssociationRef> assocs = nodeService.getParentAssocs(nodeRef);
			for(ChildAssociationRef assoc : assocs) {
				if(assoc.isPrimary() && propagatePrimaryChildAssociations
				|| !assoc.isPrimary() && propagateSecondaryChildAssociations)
				{
					nodeRefs.add(assoc.getParentRef());
				}
			}
		}
		return nodeRefs;
	}

}