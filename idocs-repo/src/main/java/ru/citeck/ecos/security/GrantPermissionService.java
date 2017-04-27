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

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Grant Permission Service - service that handles temporarily granted (restricted) permissions.
 * It collects not only ACE (object-subject-permission), but also provider of this ACE.
 * When all providers revoke their permission, the permission is deleted indeed.
 * 
 * @author Sergey Tiunov
 */
public interface GrantPermissionService {

	/**
	 * Temporarily grant permission.
	 * 
	 * @param nodeRef - object to which permission is granted
	 * @param authority - subject to which permission is granted
	 * @param permission - granted permission
	 * @param provider - identifier of provider, that granted permission
	 */
	@Auditable(parameters = {"nodeRef", "authority", "permission", "provider"})
	public void grantPermission(NodeRef nodeRef, String authority, String permission, String provider);

	/**
	 * Temporarily restrict permission.
	 * 
	 * Note: both granted and restricted permissions are cancelled by revokePermission methods.
	 * That is why if separate cancellation is necessary, you should use provider to distinct grants from restrictions.
	 * 
	 * @param nodeRef - object on which restriction is put
	 * @param authority - subject to which restriction is put
	 * @param permission - restricted permission
	 * @param provider - identifier of provider, that restricts permission
	 */
	@Auditable(parameters = {"nodeRef", "authority", "permission", "provider"})
    public void restrictPermission(NodeRef nodeRef, String authority, String permission, String provider);
    
	/**
	 * Revoke temporarily granted permission.
	 * 
	 * @param nodeRef - object from which permission is revoked
	 * @param authority - subject from which permission is revoked
	 * @param permission - revoked permission
	 * @param provider - identifier of provider, that revoked its permission
	 */
    @Auditable(parameters = {"nodeRef", "authority", "permission", "provider"})
	public void revokePermission(NodeRef nodeRef, String authority, String permission, String provider);

	/**
	 * Revoke all permissions, that were granted by the provider to this authority.
	 * 
	 * @param nodeRef - object from which permissions are revoked
	 * @param authority - subject from which permissions are revoked
	 * @param provider - identifier of provider, that revokes its permissions
	 */
    @Auditable(parameters = {"nodeRef", "authority", "provider"})
	public void revokePermission(NodeRef nodeRef, String authority, String provider);
	
	/**
	 * Revoke all permissions, that were granted by the provider.
	 * 
	 * @param nodeRef - object from which permissions are revoked
	 * @param provider - identifier of provider, that revokes its permissions
	 */
    @Auditable(parameters = {"nodeRef", "provider"})
	public void revokePermission(NodeRef nodeRef, String provider);

    /**
     * Revoke all permissions, that were granted by any provider.
     * 
     * @param nodeRef - object from which permissions are revoked
     */
    @Auditable(parameters = {"nodeRef"})
    public void revokePermission(NodeRef nodeRef);
	
    /**
     * Determines if given permission is granted by given provider.
     * 
     * @param nodeRef
     * @param authority
     * @param permission
     * @param provider
     * @return
     */
    public boolean isPermissionGranted(NodeRef nodeRef, String authority, String permission, String provider);
}
