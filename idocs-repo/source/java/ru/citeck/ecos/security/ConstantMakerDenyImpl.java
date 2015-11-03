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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * "Allow" implementation of constant maker.
 * It enables permission inheritance and denies all permissions except read.
 * Note: If permissions on the parent node are updated, it is not
 * 
 * @author Sergey Tiunov
 */
public class ConstantMakerDenyImpl implements ConstantMaker
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
		
		// deny all except read permission
		permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, Permissions.ALL_EXCEPT_READ, false);

		// enable permission inheritance
		permissionService.setInheritParentPermissions(nodeRef, true);
		
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
