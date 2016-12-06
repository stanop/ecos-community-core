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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.GrantModel;

/**
 * Simple ConfiscateService implementation.
 * Changes node owner, and can return it back then.
 * 
 * @author Sergey Tiunov
 */
public class ConfiscateServiceImpl implements ConfiscateService
{
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private OwnableService ownableService;
	private PermissionService permissionService;
	private AssociationWalker walker;
	private String confiscateToUser;

	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setWalker(AssociationWalker walker) {
		this.walker = walker;
	}

	public void setConfiscateToUser(String confiscateToUser) {
		this.confiscateToUser = confiscateToUser;
	}

	/////////////////////////////////////////////////////////////////
	//             CONFISCATE SERVICE IMPLEMENTATION               //
	/////////////////////////////////////////////////////////////////

	@Override
	public void confiscateNode(NodeRef nodeRef) {
		this.confiscateNodeImpl(nodeRef, true);
	}

	@Override
	public void confiscateNode(NodeRef nodeRef, boolean restrictInheritance) {
		this.confiscateNodeImpl(nodeRef, restrictInheritance);
	}

	@Override
	public void returnNode(NodeRef nodeRef) {
		this.returnNodeImpl(nodeRef, true);
	}

	// general confiscateNode implementation
	/*package*/ void confiscateNodeImpl(NodeRef nodeRef, boolean restrictInheritance) {
		// if node does not exist - do nothing
		if(!nodeService.exists(nodeRef)) {
			return;
		}

		// if node is already confiscated - do nothing
		if(nodeService.hasAspect(nodeRef, GrantModel.ASPECT_CONFISCATED)) {
			return;
		}
		
		// maintain permissions only for cm:cmobject subclasses
		if(!dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CMOBJECT)) {
			return;
		}
			
		// otherwise do confiscate it:
		String owner = ownableService.getOwner(nodeRef);
		boolean inherits = permissionService.getInheritParentPermissions(nodeRef);
		
		Map<QName,Serializable> properties = new HashMap<QName,Serializable>(1);
		properties.put(GrantModel.PROP_OWNER, owner);
		properties.put(GrantModel.PROP_INHERITS, inherits);
		nodeService.addAspect(nodeRef, GrantModel.ASPECT_CONFISCATED, properties);

		ownableService.setOwner(nodeRef, confiscateToUser);
		if(restrictInheritance) {
			permissionService.setInheritParentPermissions(nodeRef, false);
		}
		
		// secondary assocs receive permissions from GrantPermissionService and primary - do not
		// so we restrict permission inheritance only on secondary assocs
		this.confiscateNodeImpl(walker.getPrimaryAssocs(nodeRef), false);
		this.confiscateNodeImpl(walker.getSecondaryAssocs(nodeRef), true);
	}

	// general returnNode implementation
	/*package*/ void returnNodeImpl(NodeRef nodeRef, boolean resetOwner) {
		// if node does not exist - do nothing
		if(!nodeService.exists(nodeRef)) {
			return;
		}		
		// if node is not confiscated - do nothing
		if(!nodeService.hasAspect(nodeRef, GrantModel.ASPECT_CONFISCATED)) {
			return;
		}
		
		// otherwise return node to its owner
		if(resetOwner) {
			ownableService.setOwner(nodeRef, getOriginalOwner(nodeRef));
		}
		permissionService.setInheritParentPermissions(nodeRef, getOriginalInherits(nodeRef));
		nodeService.removeAspect(nodeRef, GrantModel.ASPECT_CONFISCATED);
		
		this.returnNodeImpl(walker.getForwardAssocs(nodeRef), resetOwner);
	}

	private String getOriginalOwner(NodeRef nodeRef) {
		return (String) nodeService.getProperty(nodeRef, GrantModel.PROP_OWNER);
	}

	private boolean getOriginalInherits(NodeRef nodeRef) {
		return !Boolean.FALSE.equals(nodeService.getProperty(nodeRef, GrantModel.PROP_INHERITS));
	}
	
	// confiscate a bunch of nodeRefs
	private void confiscateNodeImpl(Collection<NodeRef> nodeRefs, boolean restrictInheritance) {
		if(nodeRefs == null) return;
		for(NodeRef nodeRef : nodeRefs) {
			this.confiscateNodeImpl(nodeRef, restrictInheritance);
		}
	}
	
	// return a bunch of nodeRefs
	private void returnNodeImpl(Collection<NodeRef> nodeRefs, boolean resetOwner) {
		if(nodeRefs == null) return;
		for(NodeRef nodeRef : nodeRefs) {
			this.returnNodeImpl(nodeRef, resetOwner);
		}
	}
	
}
