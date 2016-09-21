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
package ru.citeck.ecos.deputy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;

import ru.citeck.ecos.model.DeputyModel;

public class AvailabilityServiceImpl implements AvailabilityService
{

	private AuthenticationService authenticationService;
	private NodeService nodeService;
	private AuthorityHelper authorityHelper;
	
	/////////////////////////////////////////////////////////////////
	//                      SPRING INTERFACE                       //
	/////////////////////////////////////////////////////////////////
	
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setAuthorityHelper(AuthorityHelper authorityHelper) {
		this.authorityHelper = authorityHelper;
	}
	
	/////////////////////////////////////////////////////////////////
	//                       ADMIN INTERFACE                       //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public boolean getUserAvailability(String userName) {
		NodeRef person = authorityHelper.needUser(userName);
		if(person == null) {
			throw new IllegalArgumentException("No such user: " + userName);
		}
		Boolean available = (Boolean) nodeService.getProperty(person, DeputyModel.PROP_AVAILABLE);
		// user is available by default
		return !Boolean.FALSE.equals(available);
	}

	@Override
	public void setUserAvailability(String userName, boolean availability) {
		NodeRef person = authorityHelper.needUser(userName);
		nodeService.setProperty(person, DeputyModel.PROP_AVAILABLE, availability);
		// note: deputy listener, related to availability change, should be called via behaviour mechanism
	}

	/////////////////////////////////////////////////////////////////
	//                  CURRENT USER INTERFACE                     //
	/////////////////////////////////////////////////////////////////
	
	@Override
	public boolean getCurrentUserAvailability() {
		return 	getUserAvailability(getCurrentUserName());
	}

	@Override
	public void setCurrentUserAvailability(boolean availability) {
		setUserAvailability(getCurrentUserName(), availability);
	}

	/////////////////////////////////////////////////////////////////
	//                       PRIVATE STUFF                         //
	/////////////////////////////////////////////////////////////////
	
	private String getCurrentUserName() {
		return authenticationService.getCurrentUserName();
	}
	
}
