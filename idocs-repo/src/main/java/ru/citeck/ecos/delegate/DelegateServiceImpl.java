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
package ru.citeck.ecos.delegate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.DelegateModel;
import ru.citeck.ecos.orgstruct.OrgStructService;

public class DelegateServiceImpl implements DelegateService
{
	private static final Log logger = LogFactory.getLog(DelegateServiceImpl.class);
	
	private AuthenticationService authenticationService;
	private AuthorityService authorityService;
	private NodeService nodeService;
	private OrgStructService orgStructService;
	private AuthorityHelper authorityHelper;
	private AvailabilityService availabilityService;
	
	private CompositeDelegateListener delegateListener = new CompositeDelegateListener();
	
	private NodeRef delegationRecordsRoot;
	private QName delegationRecordAssoc;
	private String roleGroupType;
	private String branchGroupType;

	/////////////////////////////////////////////////////////////////
	//                      SPRING INTERFACE                       //
	/////////////////////////////////////////////////////////////////
	
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setOrgStructService(OrgStructService orgStructService) {
		this.orgStructService = orgStructService;
	}
	
	public void setAuthorityHelper(AuthorityHelper authorityHelper) {
		this.authorityHelper = authorityHelper;
	}
	
	public void setAvailabilityService(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	public void setDelegationRecordsRoot(NodeRef delegationRecordsRoot) {
		this.delegationRecordsRoot = delegationRecordsRoot;
	}

	public void setDelegationRecordAssoc(QName delegationRecordAssoc) {
		this.delegationRecordAssoc = delegationRecordAssoc;
	}

	public void setRoleGroupType(String roleGroupType) {
		this.roleGroupType = roleGroupType;
	}

    public void setBranchGroupType(String branchGroupType) {
        this.branchGroupType = branchGroupType;
    }
	

	/////////////////////////////////////////////////////////////////
	//                       ADMIN INTERFACE                       //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public List<String> getUserDelegates(String userName) {
		NodeRef user = authorityHelper.needUser(userName);
		return getAuthorityNames(getAuthorityDelegates(user));
	}

	@Override
	public void addUserDelegates(String userName, List<String> delegates) {
		NodeRef user = authorityHelper.needUser(userName);
		addDelegationRecords(user, getAuthorities(delegates), false);
	}

	@Override
	public void removeUserDelegates(String userName, List<String> delegates) {
		NodeRef user = authorityHelper.needUser(userName);
		removeDelegationRecords(user, getAuthorities(delegates), false);
	}

	@Override
	public List<String> getUsersWhoHaveThisUserDelegate(String userName) {
		NodeRef user = authorityHelper.needUser(userName);
		return getAuthorityNames(getUsersDelegatedToUserImpl(user));
	}

	@Override
	public List<String> getRoleDelegates(String roleFullName) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		return getAuthorityNames(getAuthorityDelegates(role));
	}

	@Override
	public void addRoleDelegates(String roleFullName, List<String> delegates) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		addDelegationRecords(role, getAuthorities(delegates), true);		
	}

	@Override
	public void removeRoleDelegates(String roleFullName, List<String> delegates) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		removeDelegationRecords(role, getAuthorities(delegates), true);		
	}

	@Override
	public boolean isRoleDelegatedByMembers(String roleFullName) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		return isRoleDelegatedByMembers(role);
	}

	@Override
	public boolean isRoleDelegatedByUser(String roleFullName, String userName) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		NodeRef user = authorityHelper.needUser(userName);
		
		// zero - all roles are delegated by admins
		if(authorityService.isAdminAuthority(userName)) {
			return true;
		}
		
		// first - role should be delegated by members
		if(!isRoleDelegatedByMembers(role)) {
			return false;
		}
		
		// second - user should be the member of this role
		Set<String> userGroups = authorityService.getAuthoritiesForUser(userName);
		if(!userGroups.contains(roleFullName)) {
			return false;
		}
		
		// third - user should not be the delegate of this role
		NodeRef delegationRecord = this.getDelegationRecord(role, user);
		if(delegationRecord != null) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isRoleDelegatedToUser(String roleFullName, String userName) {
		NodeRef role = authorityHelper.needRole(roleFullName);
		NodeRef user = authorityHelper.needUser(userName);
		return getDelegationRecord(role, user) != null;
	}

	@Override
	public List<String> getUserRoles(String userName) {
		NodeRef user = authorityHelper.needUser(userName);
		// get all groups where user is member
		List<String> userRoles = orgStructService.getTypedGroupsForUser(userName, roleGroupType);
		
		// and exclude those, where user is delegate
		List<NodeRef> delegatedRoles = getRolesDelegatedToUserImpl(user);

		// use HashSet for fast search
		Set<NodeRef> delegatedRoleSet = new HashSet<NodeRef>(delegatedRoles.size());
		delegatedRoleSet.addAll(delegatedRoles);
		
		List<String> nonDelegatedRoles = new ArrayList<String>(delegatedRoles.size());
		for(String roleName : userRoles) {
			NodeRef role = authorityHelper.getRole(roleName);
			if(!delegatedRoleSet.contains(role)) {
				nonDelegatedRoles.add(roleName);
			}
		}
		return nonDelegatedRoles;
	}

    @Override
    public List<String> getUserBranches(String userName) {
        return orgStructService.getTypedGroupsForUser(userName, branchGroupType);
    }

    @Override
	public List<String> getRoleMembers(String roleFullName) {
		// get all members of the role:
		Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, roleFullName, false);
		
		// and exclude those, that are delegates
		List<String> delegates = this.getRoleDelegates(roleFullName);
		Set<String> delegatesSet = new HashSet<String>(delegates.size());
		delegatesSet.addAll(delegates);
		
		List<String> fullMembers = new ArrayList<String>(members.size());
		for(String member : members) {
			if(!delegatesSet.contains(member)) {
				fullMembers.add(member);
			}
		}
		
		return fullMembers;
	}
	
	@Override
	public List<String> getRolesDelegatedByUser(String userName) {
		NodeRef user = authorityHelper.needUser(userName);
		// get all groups where user is member
		List<String> userRoles = orgStructService.getTypedGroupsForUser(userName, roleGroupType);
		
		// get those, where user is delegate
		List<NodeRef> delegatedRoles = getRolesDelegatedToUserImpl(user);

		// use HashSet for fast search
		Set<NodeRef> delegatedRoleSet = new HashSet<NodeRef>(delegatedRoles.size());
		delegatedRoleSet.addAll(delegatedRoles);
		
		List<String> managedRoles = new ArrayList<String>(userRoles.size());
		for(String roleName : userRoles) {
			NodeRef role = authorityHelper.getRole(roleName);
			// role should be not delegated to user
			// and it should be delegated (managed) by members
			if(!delegatedRoles.contains(role) && isRoleDelegatedByMembers(role)) {
				managedRoles.add(roleName);
			}
		}
		return managedRoles;
	}

	@Override
	public List<String> getRolesDelegatedToUser(String userName) {
		NodeRef user = authorityHelper.needUser(userName);
		return getAuthorityNames(getRolesDelegatedToUserImpl(user));
	}
	
	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////

	@Override
	public List<String> getCurrentUserDelegates() {
        return getUserDelegates(getCurrentUserName());
	}

	@Override
	public void addCurrentUserDelegates(List<String> delegates) {
		addUserDelegates(getCurrentUserName(), delegates);
	}

	@Override
	public void removeCurrentUserDelegates(List<String> delegates) {
		removeUserDelegates(getCurrentUserName(), delegates);
	}

	@Override
	public boolean isRoleDelegatedByCurrentUser(final String roleFullName) {
        return isRoleDelegatedByUser(roleFullName, getCurrentUserName());
	}

	@Override
	public boolean isRoleDelegatedToCurrentUser(final String roleFullName) {
        return isRoleDelegatedToUser(roleFullName, getCurrentUserName());
	}

	@Override
	public List<String> getCurrentUserRoles() {
        return getUserRoles(getCurrentUserName());
	}

    @Override
    public List<String> getCurrentUserBranches() {
        return getUserBranches(getCurrentUserName());
    }

    @Override
	public List<String> getRolesDelegatedByCurrentUser() {
        return getRolesDelegatedByUser(getCurrentUserName());
	}

	@Override
	public List<String> getRolesDelegatedToCurrentUser() {
		return getRolesDelegatedToUser(getCurrentUserName());
	}
	
	/////////////////////////////////////////////////////////////////
	//                     LISTENER INTERFACE                      //
	/////////////////////////////////////////////////////////////////
	
	public void addDelegateListener(DelegateListener listener) {
		delegateListener.addDelegateListener(listener);
	}

	@Override
	public void userAvailabilityChanged(String userName) {
		authorityHelper.needUser(userName);
		boolean available = availabilityService.getUserAvailability(userName);
		List<String> userRoles = getUserRoles(userName);
		List<String> delegatedRoles = getRolesDelegatedToUser(userName);

		if(available) {
			delegateListener.onUserAvailable(userName);
			for(String role : userRoles) {
				delegateListener.onRoleMemberAvailable(role, userName);
			}
			for(String role : delegatedRoles) {
				delegateListener.onRoleDelegateAvailable(role, userName);
			}
		} else {
			delegateListener.onUserUnavailable(userName);
			for(String role : userRoles) {
				delegateListener.onRoleMemberUnavailable(role, userName);
			}
			for(String role : delegatedRoles) {
				delegateListener.onRoleDelegateUnavailable(role, userName);
			}
		}
	}

	@Override
	public void userMembershipChanged(String userName, String groupFullName) {
		if(!authorityHelper.isRole(groupFullName)) {
			return;
		}
		authorityHelper.needUser(userName);
		
		// check whether user is in group
		Set<String> userGroups = authorityService.getAuthoritiesForUser(userName);

		if(userGroups.contains(groupFullName)) {
			delegateListener.onRoleMemberAvailable(groupFullName, userName);
		} else {
			delegateListener.onRoleMemberUnavailable(groupFullName, userName);
		}
	}

	/////////////////////////////////////////////////////////////////
	//                       PRIVATE STUFF                         //
	/////////////////////////////////////////////////////////////////
	
	private String getCurrentUserName() {
		return authenticationService.getCurrentUserName();
	}
	
	private List<NodeRef> getAuthorities(Collection<String> authorityNames) {
		List<NodeRef> authorities = new ArrayList<NodeRef>(authorityNames.size());
		for(String authorityName : authorityNames) {
			NodeRef authority = authorityHelper.getAuthority(authorityName);
			if(authority != null) {
				authorities.add(authority);
			}
		}
		return authorities;
	}
	
	private List<String> getAuthorityNames(Collection<NodeRef> authorities) {
		List<String> authorityNames = new ArrayList<String>(authorities.size());
		for(NodeRef authority : authorities) {
			String authorityName = authorityHelper.getAuthorityName(authority);
			if(authorityName != null) {
				authorityNames.add(authorityName);
			}
		}
		return authorityNames;
	}
	
	// get delegates for specified authority
	private List<NodeRef> getAuthorityDelegates(NodeRef authority) {
		List<NodeRef> delegationRecords = 
				searchDelegationRecords(DelegateModel.ASSOC_DELEGATED_AUTHORITY, authority);
		Map<NodeRef, NodeRef> delegateMap = mapDelegationRecordsAssocs(delegationRecords, 
				DelegateModel.ASSOC_DELEGATE);
		List<NodeRef> delegates = new ArrayList<NodeRef>(delegateMap.size());
		delegates.addAll(delegateMap.values());
		return delegates;
	}
	
	// get roles delegated to user - without checks
	private List<NodeRef> getRolesDelegatedToUserImpl(NodeRef user) {
		List<NodeRef> delegationRecords = 
				searchDelegationRecords(DelegateModel.ASSOC_DELEGATE, user);
		Map<NodeRef, NodeRef> delegateMap = mapDelegationRecordsAssocs(delegationRecords, 
				DelegateModel.ASSOC_DELEGATED_AUTHORITY);
		return filterAuthoritiesByType(delegateMap.values(), ContentModel.TYPE_AUTHORITY_CONTAINER);
	}

	private List<NodeRef> getUsersDelegatedToUserImpl(NodeRef user) {
		List<NodeRef> delegationRecords =
				searchDelegationRecords(DelegateModel.ASSOC_DELEGATE, user);
		Map<NodeRef, NodeRef> delegateMap = mapDelegationRecordsAssocs(delegationRecords,
				DelegateModel.ASSOC_DELEGATED_AUTHORITY);
		return filterAuthoritiesByType(delegateMap.values(), ContentModel.TYPE_PERSON);
	}
	
	private List<NodeRef> filterAuthoritiesByType(Collection<NodeRef> authorities, QName requiredType) {
		List<NodeRef> filtered = new ArrayList<NodeRef>(authorities.size());
		for(NodeRef authority : authorities) {
			QName type = nodeService.getType(authority);
			if(requiredType.equals(type)) {
				filtered.add(authority);
			}
		}
		return filtered;
	}
	
	// search delegation records by association
	private List<NodeRef> searchDelegationRecords(QName searchAssoc, NodeRef assocValue) {
		if(!nodeService.exists(assocValue)) {
			return Collections.emptyList();
		}
		List<AssociationRef> assocs = nodeService.getSourceAssocs(assocValue, searchAssoc);
		List<NodeRef> delegationRecords = new ArrayList<NodeRef>(assocs.size());
		for(AssociationRef assoc : assocs) {
			delegationRecords.add(assoc.getSourceRef());
		}
		return delegationRecords;
	}
	
	// get map of delegationRecords assocs
	private Map<NodeRef, NodeRef> mapDelegationRecordsAssocs(Collection<NodeRef> delegationRecords, 
			QName assocName) 
	{
		Map<NodeRef, NodeRef> results = new HashMap<NodeRef, NodeRef>(delegationRecords.size());
		for(NodeRef delegationRecord : delegationRecords) {
			List<AssociationRef> assocs = nodeService.getTargetAssocs(delegationRecord, assocName);
			if(assocs == null || assocs.size() == 0) {
				continue;
			}
			
			NodeRef authorityRef = assocs.get(0).getTargetRef();
			if(authorityRef != null && nodeService.exists(authorityRef)) {
				results.put(delegationRecord, authorityRef);
			}
		}
		return results;
	}
	
	// get delegation record or null, if no such
	private NodeRef getDelegationRecord(NodeRef delegatedAuthority, NodeRef delegate) {
		List<AssociationRef> assocs = nodeService.getSourceAssocs(delegate, DelegateModel.ASSOC_DELEGATE);
		for(AssociationRef assoc : assocs) {
			NodeRef delegateRecord = assoc.getSourceRef();
			List<AssociationRef> authorityRefs = nodeService.getTargetAssocs(delegateRecord, DelegateModel.ASSOC_DELEGATED_AUTHORITY);
			if(authorityRefs == null || authorityRefs.size() == 0) {
				continue;
			}
			if(delegatedAuthority.equals(authorityRefs.get(0).getTargetRef())) {
				return delegateRecord;
			}
		}
		return null;
	}
	
	// add delegation record and return nodeRef to it
	private NodeRef addDelegationRecord(NodeRef delegatedAuthority, 
			NodeRef delegate, boolean delegatedRole) 
	{
		// first search for existing record:
		NodeRef delegationRecord = getDelegationRecord(delegatedAuthority, delegate);
		// if it exists - simply return it
		if(delegationRecord != null && nodeService.exists(delegationRecord)) {
			return delegationRecord;
		}
		
		// otherwise create new delegationRecord
		QName assocName = QName.createQName(DelegateModel.NAMESPACE, GUID.generate());
		ChildAssociationRef childAssocRef = nodeService.createNode(delegationRecordsRoot, 
				delegationRecordAssoc, assocName, DelegateModel.TYPE_DELEGATION_RECORD);
		delegationRecord = childAssocRef.getChildRef();
		
		nodeService.createAssociation(delegationRecord, delegatedAuthority, DelegateModel.ASSOC_DELEGATED_AUTHORITY);
		nodeService.createAssociation(delegationRecord, delegate, DelegateModel.ASSOC_DELEGATE);
		
		// and call listener
		String authorityName = authorityHelper.getAuthorityName(delegatedAuthority);
		String delegateName = authorityHelper.getAuthorityName(delegate);
		if(delegatedRole) {
			delegateListener.onRoleDelegateAvailable(authorityName, delegateName);
		} else {
			delegateListener.onUserDelegateAvailable(authorityName, delegateName);
		}
		
		return childAssocRef.getChildRef();
	}
	
	// add delegation records and return nodeRefs of them
	private List<NodeRef> addDelegationRecords(NodeRef delegatedAuthority, 
			Collection<NodeRef> delegates, boolean delegatedRole) 
	{
		List<NodeRef> delegationRecords = new ArrayList<NodeRef>(delegates.size());
		// then add all delegation records
		for(NodeRef delegate : delegates) {
			NodeRef delegationRecord = addDelegationRecord(delegatedAuthority, delegate, delegatedRole);
			delegationRecords.add(delegationRecord);
		}
		return delegationRecords;
	}

	// remove specified delegation record, if exists
	private void removeDelegationRecord(NodeRef delegatedAuthority, 
			NodeRef delegate, boolean delegatedRole)
	{
		// first search for existing record:
		NodeRef delegationRecord = getDelegationRecord(delegatedAuthority, delegate);
		// if it is null or does not exist - there is nothing to do
		if(delegationRecord == null || !nodeService.exists(delegationRecord)) {
			return;
		}

		// otherwise perform deletion
		nodeService.deleteNode(delegationRecord);
		
		// and call listener
		String authorityName = authorityHelper.getAuthorityName(delegatedAuthority);
		String delegateName = authorityHelper.getAuthorityName(delegate);
		if(delegatedRole) {
			delegateListener.onRoleDelegateUnavailable(authorityName, delegateName);
		} else {
			delegateListener.onUserDelegateUnavailable(authorityName, delegateName);
		}
	}
	
	// remove specified delegation records, if any
	private void removeDelegationRecords(NodeRef delegatedAuthority, 
			Collection<NodeRef> delegates, boolean delegatedRole)
	{
		// then remove all delegation records
		for(NodeRef delegate : delegates) {
			removeDelegationRecord(delegatedAuthority, delegate, delegatedRole);
		}
	}
	
	private boolean isRoleDelegatedByMembers(NodeRef role) {
		Boolean managed = (Boolean) nodeService.getProperty(role, DelegateModel.PROP_MANAGED_BY_MEMBERS);
		// role is not managed by members by default
		return Boolean.TRUE.equals(managed);
	}

}
