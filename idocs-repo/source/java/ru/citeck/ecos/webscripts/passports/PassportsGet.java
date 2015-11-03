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
package ru.citeck.ecos.webscripts.passports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.model.PassportModel;

public class PassportsGet extends DeclarativeWebScript 
{
	private static final String PARAM_USERNAME = "userName";
	private static final String MODEL_PASSPORTS = "passports";
	private static final String MODEL_NODE = "node";
	private static final String MODEL_CREATED = "created";
	
	private NodeService nodeService;
	private PersonService personService;
	
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    	Map<String, Object> model = new HashMap<String, Object>();
    	
    	String userName = req.getParameter(PARAM_USERNAME);
    	NodeRef person = null;
    	try {
    		person = personService.getPerson(userName);
    	} catch(NoSuchPersonException e) {
    		status.setCode(Status.STATUS_NOT_FOUND, "Can not find person " + userName);
    		return null;
    	}
    	
    	List<AssociationRef> passportRefs = nodeService.getSourceAssocs(person, PassportModel.ASSOC_PERSON);
    	
    	@SuppressWarnings("rawtypes")
		List<Map> passports = new ArrayList<Map>(passportRefs.size());
    	for(AssociationRef passportRef : passportRefs) {
    		Map<String, Object> passportModel = new HashMap<String, Object>();
    		passportModel.put(MODEL_NODE, passportRef.getSourceRef());
    		passportModel.put(MODEL_CREATED, nodeService.getProperty(passportRef.getSourceRef(), ContentModel.PROP_CREATED));
    		passports.add(passportModel);
    	}
    	
    	model.put(MODEL_PASSPORTS, passports);
		return model;
    }

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

}
