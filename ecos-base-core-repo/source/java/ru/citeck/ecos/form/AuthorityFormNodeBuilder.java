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
package ru.citeck.ecos.form;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

public class AuthorityFormNodeBuilder extends AbstractFormNodeBuilder
{
	private AuthorityService authorityService;
	private static String PROP_NAME = "prop_cm_authorityName";
	
	@Override
	public NodeRef createNode(TypeDefinition typeDef, FormData data)
	{
		// get parent group, if any:
		FieldData destination = data.getFieldData(DESTINATION);
		String parentAuthority = destination == null ? null : (String) destination.getValue();
		data.removeFieldData(DESTINATION);
		
		// get authority name:
		FieldData nameData = data.getFieldData(PROP_NAME);
		String name = nameData == null ? null : (String) nameData.getValue();
		if (name == null || name.length() == 0) {
			throw new FormException("Authority name should be specified on form");
		}
		data.removeFieldData(PROP_NAME);
		
		// check if authority exists:
		if(authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, name))) {
			throw new AlfrescoRuntimeException("Can't create group " + name + ", because it already exists");
		}
		
		// create authority:
		String fullName = authorityService.createAuthority(AuthorityType.GROUP, name);
		if(fullName == null) {
			return null;
		}
		
		// if there is parent group - put new group inside parent:
		if(parentAuthority != null && parentAuthority.length() > 0) {
			authorityService.addAuthority(parentAuthority, fullName);
		}
		
		// finally return authority nodeRef:
		return authorityService.getAuthorityNodeRef(fullName);
	}
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
}
