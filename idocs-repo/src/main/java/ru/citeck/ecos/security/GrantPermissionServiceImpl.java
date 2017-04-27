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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

import ru.citeck.ecos.model.GrantModel;

public class GrantPermissionServiceImpl implements 
	GrantPermissionService
{
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private PermissionService permissionService;
	private AssociationWalker walker;
	
	// null encodes all or any (authority, permission, provider)
	private static final String ALL = null;
	private static final String ANY = null;
	
	// argument names
	private static final String PROVIDER = "provider";
	private static final String AUTHORITY = "authority";
	private static final String PERMISSION = "permission";
		
	/////////////////////////////////////////////////////////////////
	//                     SPRING INTERFACE                        //
	/////////////////////////////////////////////////////////////////

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setWalker(AssociationWalker walker) {
		this.walker = walker;
	}

	/////////////////////////////////////////////////////////////////
	//           GRANT PERMISSION SERVICE IMPLEMENTATION           //
	/////////////////////////////////////////////////////////////////

	@Override
	public void grantPermission(NodeRef nodeRef, String authority, String permission, String provider) 
	{
		ParameterCheck.mandatory(PROVIDER, provider);
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PERMISSION, permission);
		grantPermissionImpl(nodeRef, authority, permission, provider, true);
	}

	@Override
	public void restrictPermission(NodeRef nodeRef, String authority, String permission, String restrictor) 
	{
		ParameterCheck.mandatory(PROVIDER, restrictor);
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PERMISSION, permission);
		grantPermissionImpl(nodeRef, authority, permission, restrictor, false);
	}

	@Override
	public void revokePermission(NodeRef nodeRef, String authority, String permission, String provider) 
	{
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PERMISSION, permission);
		ParameterCheck.mandatory(PROVIDER, provider);
		revokePermissionImpl(nodeRef, authority, permission, provider);
	}

	@Override
	public void revokePermission(NodeRef nodeRef, String authority, String provider) 
	{
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PROVIDER, provider);
		revokePermissionImpl(nodeRef, authority, ALL, provider);
	}

	@Override
	public void revokePermission(NodeRef nodeRef, String provider) 
	{
		ParameterCheck.mandatory(PROVIDER, provider);
		revokePermissionImpl(nodeRef, ALL, ALL, provider);
	}
	
	@Override
	public void revokePermission(NodeRef nodeRef) {
		revokePermissionImpl(nodeRef, ALL, ALL, ALL);
	}
	
	@Override
	public boolean isPermissionGranted(NodeRef nodeRef, String authority, String permission, String provider)
	{
		ParameterCheck.mandatory(PROVIDER, provider);
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PERMISSION, permission);
		return isPermissionGrantedImpl(nodeRef, authority, permission, provider);
	}

	// check if permission is granted on node
	private boolean isPermissionGrantedImpl(NodeRef nodeRef, String authority, String permission, String provider)
	{
		// check if there is such nodeRef:
		if(!nodeService.exists(nodeRef)) return false;

		// first check the permissions on the node itself:
		NodeRef grantedPermission = getPermissionObject(nodeRef, authority, permission, provider);
		
		// if it is granted - we are done
		if(grantedPermission != null) {
			return true;
		}
		
		// otherwise it is not granted
		// we do not do indirect search here, because if permission is granted, the permission object should be already there
		return false; 
	}
	
	// check if permission is granted on node's parents
	private boolean isPermissionGrantedIndirect(NodeRef nodeRef, String authority, String permission, String provider)
	{
		return isPermissionGrantedImpl(walker.getBackwardAssocs(nodeRef), authority, permission, provider);
	}
	
	private NodeRef getPermissionObject(NodeRef nodeRef, String authority, String permission, String provider) 
	{
		// get association qname:
		QName permissionAssocQName = getAssocQName(authority, permission, provider);
		
		// check if there is such permission:
		List<ChildAssociationRef> grantedPermissions = nodeService.getChildAssocs(nodeRef, GrantModel.ASSOC_PERMISSIONS, permissionAssocQName);
		
		if(grantedPermissions != null && grantedPermissions.size() > 0) {
			return grantedPermissions.get(0).getChildRef();
		} else {
			return null;
		}
	}
	
	// grant permission implementation
	private void grantPermissionImpl(NodeRef nodeRef, String authority, String permission, String provider, Boolean allow) 
	{
		// all arguments should be provided
		ParameterCheck.mandatory(PROVIDER, provider);
		ParameterCheck.mandatory(AUTHORITY, authority);
		ParameterCheck.mandatory(PERMISSION, permission);
		
		// check if there is such nodeRef:
		if(!nodeService.exists(nodeRef)) return;

		// do not grant permissions on objects, other than cm:cmobject
		// (i.e. do not spread it on authorities, etc.)
		if(!dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CMOBJECT)) {
			return;
		}
		
		NodeRef grantedPermission = getPermissionObject(nodeRef, authority, permission, provider);
		
		// if such permission is there already:
		if(grantedPermission != null) {
			return;
		}

		// add GRANTED aspect to it:
		if(!nodeService.hasAspect(nodeRef, GrantModel.ASPECT_GRANTED)) {
			nodeService.addAspect(nodeRef, GrantModel.ASPECT_GRANTED, null);
		}
		// add permission nodeRef
		Map<QName, Serializable> permissionProps = new HashMap<QName, Serializable>();
		permissionProps.put(GrantModel.PROP_PERMISSION, permission);
		permissionProps.put(GrantModel.PROP_AUTHORITY, authority);
		permissionProps.put(GrantModel.PROP_PROVIDER, provider);
		permissionProps.put(GrantModel.PROP_ALLOW, allow);
		QName permissionAssocQName = getAssocQName(authority, permission, provider);
		ChildAssociationRef permissionRef = nodeService.createNode(nodeRef, GrantModel.ASSOC_PERMISSIONS, permissionAssocQName, GrantModel.TYPE_PERMISSION, permissionProps);
		
		if(permissionRef != null) {
			grantedPermission = permissionRef.getChildRef();
		}
		
		// actually add permission:
		if(grantedPermission != null) {
			permissionService.setPermission(nodeRef, authority, permission, allow != null ? allow : true);
		}
		
		// propagate grant through associations:
		grantPermissionImpl(walker.getForwardAssocs(nodeRef), authority, permission, provider, allow);
	}
	
	// revoke permission implementation
	// authority, permission and provider can be concrete strings or ANY
	private void revokePermissionImpl(NodeRef nodeRef, String authority, String permission, String provider) 
	{
		// check if there is such nodeRef:
		if(!nodeService.exists(nodeRef)) return;
	
		revokePermissionImpl(nodeRef, nodeRef, authority, permission, provider);
	}
	
	// revoke permission implementation with split parameters
	// grant:permission objects are taken from sourceNodeRef, 
	// but permissions are revoked from targetNodeRef
	private void revokePermissionImpl(NodeRef sourceNodeRef, NodeRef targetNodeRef, 
			String authority, String permission, String provider) 
	{
		boolean sourceEqualsTarget = sourceNodeRef.equals(targetNodeRef);
		
		// names of permissions granted by this provider:
		QNamePattern providerPattern = getAssocQNamePattern(authority, permission, provider);
		
		// names of permissions granted by all providers:
		QNamePattern anyProviderPattern = getAssocQNamePattern(authority, permission, ANY);
		
		// get all granted permissions:
		List<ChildAssociationRef> grantedPermissions = nodeService.getChildAssocs(sourceNodeRef, GrantModel.ASSOC_PERMISSIONS, anyProviderPattern);
		if(!sourceEqualsTarget) {
			grantedPermissions.addAll(nodeService.getChildAssocs(targetNodeRef, GrantModel.ASSOC_PERMISSIONS, anyProviderPattern));
		}
		
		// get granted permissions and revoked permissions
		Set<Permission> grantedPermissionInfo = new HashSet<Permission>();
		Set<Permission> revokedPermissionInfo = new HashSet<Permission>();
		for(ChildAssociationRef grantedPermission : grantedPermissions) {
			Permission perm = new Permission(grantedPermission);
			if(providerPattern.isMatch(grantedPermission.getQName())) {
				revokedPermissionInfo.add(perm);
			} else {
				grantedPermissionInfo.add(perm);
			}
		}
		
		Set<AccessPermission> setPermissions = permissionService.getAllSetPermissions(targetNodeRef);
		
		// actually remove permissions, that are not granted by any provider:
		for(Permission revokedPermission : revokedPermissionInfo) {
			
			String authorityName = revokedPermission.getAuthority();
			String permissionName = revokedPermission.getPermission();
			
			// find permission
			AccessPermission setPermission = findPermission(setPermissions, authorityName, permissionName);
			if(setPermission == null || !setPermission.isSetDirectly()) {
				continue;
			}
			
			// if it is not granted by this provider in some other ways:
			if(permission == ANY || !isPermissionGrantedIndirect(targetNodeRef, authorityName, permissionName, provider))
			{
				
				// remove granted permissions
				ChildAssociationRef permissionRef = revokedPermission.getPermissionRef();
				if(sourceEqualsTarget || permissionRef.getParentRef().equals(targetNodeRef)) {
					nodeService.removeChildAssociation(permissionRef);
				}

				// if additionally it is not granted by some other provider
				if(!grantedPermissionInfo.contains(revokedPermission)) 
				{
					// then actually remove permission:
					permissionService.deletePermission(targetNodeRef, authorityName, permissionName);
				}
			}
			
		}

		// propagate revoke through associations:
		revokePermissionImpl(walker.getForwardAssocs(targetNodeRef), authority, permission, provider);
	}

	private AccessPermission findPermission(
			Set<AccessPermission> accessPermissions, String authority,
			String permission) {
		for(AccessPermission perm : accessPermissions) {
			if(perm.getAuthority().equals(authority) && perm.getPermission().equals(permission)) {
				return perm;
			}
		}
		return null;
	}
	
	// grant permission to group of nodes
	private void grantPermissionImpl(Collection<NodeRef> nodeRefs, String authority, String permission, String provider, boolean allow) {
		if(nodeRefs == null) return;
		for(NodeRef nodeRef : nodeRefs) {
			grantPermissionImpl(nodeRef, authority, permission, provider, allow);
		}
	}
	
	// revoke permission from group of nodes
	private void revokePermissionImpl(Collection<NodeRef> nodeRefs, String authority, String permission, String provider) {
		if(nodeRefs == null) return;
		for(NodeRef nodeRef : nodeRefs) {
			revokePermissionImpl(nodeRef, authority, permission, provider);
		}
	}
	
	// check permission in group of nodes
	private boolean isPermissionGrantedImpl(Collection<NodeRef> nodeRefs, String authority, String permission, String provider) {
		if(nodeRefs == null) return false;
		for(NodeRef nodeRef : nodeRefs) {
			if(isPermissionGrantedImpl(nodeRef, authority, permission, provider)) {
				return true;
			}
		}
		return false;
	}
	
	private static final String ANY_PATTERN = "[^/]+";
	private static final String DELIM = "/";
	private String getAssocName(String authority, String permission, String provider)
	{
		if(authority == ANY) authority = ANY_PATTERN;
		if(permission == ANY) permission = ANY_PATTERN;
		if(provider == ANY) provider = ANY_PATTERN;
		return provider + DELIM + authority + DELIM + permission;
	}
	
	private QName getAssocQName(String authority, String permission, String provider)
	{
		return QName.createQName(GrantModel.NAMESPACE, getAssocName(authority, permission, provider));
	}
	
	private QNamePattern getAssocQNamePattern(String authority, String permission, String provider) 
	{
		if(authority == ANY || permission == ANY || provider == ANY) {
			return new RegexQNamePattern(GrantModel.NAMESPACE, "^" + getAssocName(authority, permission, 
					provider == ANY ? ANY : provider.replaceAll("([$])", "\\\\$1")) + "$");
		} else {
			return getAssocQName(authority, permission, provider);
		}
	}
	
	private class Permission {
		
		private String authority, permission;
		private ChildAssociationRef permissionRef;

		public Permission(ChildAssociationRef permissionRef) {
			this.permissionRef = permissionRef;
			this.authority = (String) nodeService.getProperty(permissionRef.getChildRef(), GrantModel.PROP_AUTHORITY);
			this.permission = (String) nodeService.getProperty(permissionRef.getChildRef(), GrantModel.PROP_PERMISSION);
		}

		public String getAuthority() {
			return authority;
		}

		public String getPermission() {
			return permission;
		}
		
		public ChildAssociationRef getPermissionRef() {
			return permissionRef;
		}

		@Override
		public String toString() {
			return authority + "[" + permission + "]";
		}
		
		@Override 
		public boolean equals(Object x) {
			if(x instanceof Permission) {
				Permission that = (Permission)x;
				return this == that || this.authority.equals(that.authority) && this.permission.equals(that.permission);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return authority.hashCode() ^ permission.hashCode();
		}


	}
	
	/////////////////////////////////////////////////////////////////
	//             GRANTED ASPECT BEHAVIOUR INTERFACE              //
	/////////////////////////////////////////////////////////////////
	
	// grant parent permissions to child
	/*package*/ void grantPermissionsImpl(final NodeRef child, final NodeRef parent) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				// check existence:
				if(!nodeService.exists(parent) || !nodeService.exists(child)) {
					return null;
				}

				// get all permissions, granted to parent:
				List<ChildAssociationRef> parentPermissions = nodeService.getChildAssocs(parent, GrantModel.ASSOC_PERMISSIONS, RegexQNamePattern.MATCH_ALL);
				
				for(ChildAssociationRef permissionRef : parentPermissions) {
					Map<QName, Serializable> permission = nodeService.getProperties(permissionRef.getChildRef());
					grantPermissionImpl(child, 
							(String) permission.get(GrantModel.PROP_AUTHORITY), 
							(String) permission.get(GrantModel.PROP_PERMISSION), 
							(String) permission.get(GrantModel.PROP_PROVIDER),
							(Boolean) permission.get(GrantModel.PROP_ALLOW));
				}
				return null;
			}
		});
	}

	// revoke parent permissions from child
	/*package*/ void revokePermissionsImpl(final NodeRef child, final NodeRef parent) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				// check existence:
				if(!nodeService.exists(parent) || !nodeService.exists(child)) {
					return null;
				}
				
				// get all permissions, granted to parent:
				List<ChildAssociationRef> parentPermissions = nodeService.getChildAssocs(parent, GrantModel.ASSOC_PERMISSIONS, RegexQNamePattern.MATCH_ALL);
				
				for(ChildAssociationRef permissionRef : parentPermissions) {
					Map<QName, Serializable> permission = nodeService.getProperties(permissionRef.getChildRef());
					revokePermissionImpl(child, 
							(String) permission.get(GrantModel.PROP_AUTHORITY), 
							(String) permission.get(GrantModel.PROP_PERMISSION), 
							(String) permission.get(GrantModel.PROP_PROVIDER));
				}
				return null;
			}
		});
	}
	
	// revoke permission from copied node
	/*package*/ void revokePermissionsOnCopy(final NodeRef source, final NodeRef target) {
		AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				// check existence:
				if(!nodeService.exists(source) || nodeService.hasAspect(source, ContentModel.ASPECT_WORKING_COPY)) {
					return null;
				}
				if(!nodeService.exists(target) || nodeService.hasAspect(target, ContentModel.ASPECT_WORKING_COPY)) {
					return null;
				}
				
				// if copy is not deep, grant:permission objects are not copied and are taken from source
				// if copy is deep, grant:permission objects are copied and are taken from both source and target
				revokePermissionImpl(source, target, ALL, ALL, ALL);
				return null;
			}
		});
	}

}
