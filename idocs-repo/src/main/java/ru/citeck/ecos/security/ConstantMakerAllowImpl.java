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

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * "Allow" implementation of constant maker.
 * It sets all inherited allowed read permissions directly and then disables inheritance.
 * Note: If permissions on the parent node are updated, it is not
 * 
 * @author Sergey Tiunov
 */
public class ConstantMakerAllowImpl implements ConstantMaker
{
	private NodeService nodeService;
	private PermissionService permissionService;
    private NodeOwnerDAO ownerDAO;

	@Override
	public void makeConstant(NodeRef nodeRef) {
		
		// do nothing for unexistant nodes
		if(!nodeService.exists(nodeRef)) {
			return;
		}
		
		// enable permission inheritance
		permissionService.setInheritParentPermissions(nodeRef, true);

		// get all (inherited) permissions
		Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
		
		// process inherited permissions
		// add only read permissions to node
		if(permissions != null) {
			for(AccessPermission permission : permissions) {
				// do nothing about direct permissions
				if(permission.isSetDirectly()) {
					continue;
				}
				// from any permission leave only its read part
				// TODO make the intersection of inherited permission and read permission
				// TODO because not every permission has the leave part
				String authority = permission.getAuthority();
				boolean allow = AccessStatus.ALLOWED.equals(permission.getAccessStatus());
				if(allow) {
					permissionService.setPermission(nodeRef, authority, PermissionService.READ, allow);
				}
			}
		}
		
		// disable permission inheritance
		permissionService.setInheritParentPermissions(nodeRef, false);
		
		// make system owner
		ownerDAO.setOwner(nodeRef, AuthenticationUtil.getSystemUserName());
		
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

    public void setNodeOwnerDAO(NodeOwnerDAO ownerDAO) {
        this.ownerDAO = ownerDAO;
    }

}
