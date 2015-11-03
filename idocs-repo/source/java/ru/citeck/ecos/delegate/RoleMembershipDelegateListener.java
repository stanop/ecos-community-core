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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Delegate Listener, that processes role membership changes.
 * 
 * If role member (not delegate) becomes unavailable, 
 *  this listener delegates the role to its delegates (i.e. adds delegates to the role).
 *  
 * If role member (not delegate) becomes available,
 *  this listener "undelegates" the role from its delegates (i.e. removes delegates from the role).
 * 
 * @author Sergey Tiunov
 */
public class RoleMembershipDelegateListener extends AbstractDelegateListener
{
	private AuthorityService authorityService;
	private AvailabilityService availabilityService;
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setAvailabilityService(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@Override
	public void onRoleMemberAvailable(String roleFullName, String memberName) {
		onRoleMemberAvailabilityChanged(roleFullName, memberName, false);
	}

	@Override
	public void onRoleMemberUnavailable(String roleFullName, String memberName) {
		onRoleMemberAvailabilityChanged(roleFullName, memberName, true);
	}
	
	private void onRoleMemberAvailabilityChanged(String roleFullName, String memberName, boolean delegate) {
		
		// get all current users in role
		Set<String> allUsers = authorityService.getContainedAuthorities(AuthorityType.USER, roleFullName, false);
		
		// get delegates of role
		List<String> delegatesList = delegateService.getRoleDelegates(roleFullName);
		
		Set<String> delegateUsers = new HashSet<String>(delegatesList.size());
		delegateUsers.addAll(delegatesList);
		
		Set<String> memberUsers = new HashSet<String>(allUsers.size());
		for(String user : allUsers) {
			if(!delegateUsers.contains(user)) {
				memberUsers.add(user);
			}
		}
		
		if(delegate && isRoleDelegationNecessary(roleFullName, memberUsers, delegateUsers)) {
			delegate(roleFullName, delegateUsers);
		}
		
		if(!delegate && !isRoleDelegationNecessary(roleFullName, memberUsers, delegateUsers)){
			undelegate(roleFullName, delegateUsers);
		}
		
	}
	
	private void delegate(String roleFullName, Set<String> delegateUsers) {
		
		for(String delegate : delegateUsers) {
			authorityService.addAuthority(roleFullName, delegate);
		}
		
	}
	
	private void undelegate(String roleFullName, Set<String> delegateUsers) {
		
		for(String delegate : delegateUsers) {
			authorityService.removeAuthority(roleFullName, delegate);
		}
		
	}

	// TODO encapsulate this decision into abstract predicate
	private boolean isRoleDelegationNecessary(String roleFullName, Set<String> memberUsers, Set<String> delegateUsers) {
		// check availability of full members
		for(String user : memberUsers) {
			if(availabilityService.getUserAvailability(user)) {
				return false;
			}
		}
		return true;
	}
	
}
