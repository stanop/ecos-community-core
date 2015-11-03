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
package ru.citeck.ecos.webscripts.security;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.model.SecurityModel;

import java.io.IOException;

public class DigitalSignaturePut extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(DigitalSignaturePut.class);

	private NodeService nodeService;
	private PersonService personService;
    private VersionService versionService;

    public static interface ParamsDefinition {
        public static String NODE_REF = "nodeRef";
        public static String SIGNER = "signer";
        public static String SIGNATURE_VALUE = "sign";
    }

    @Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
        String sNodeRef = req.getParameter(ParamsDefinition.NODE_REF);
		NodeRef nodeRef = new NodeRef(sNodeRef);
        if (sNodeRef.contains("versionStore")) {
            nodeRef = VersionUtil.convertNodeRef(nodeRef);
        }
	    if(!nodeService.hasAspect(nodeRef, SecurityModel.ASPECT_SIGNABLE)) {
		    try {
			    throw new BusinessException("Illegal sign operation");
		    } catch (BusinessException e) {
			    logger.error(e.getStackTrace());
			    return;
		    }
	    }
		nodeService.setProperty(
                nodeRef,
                SecurityModel.PROP_SIGNATURE_VALUE,
                req.getParameter(ParamsDefinition.SIGNATURE_VALUE)
        );
		nodeService.setProperty(
                nodeRef,
                SecurityModel.PROP_SIGNER,
                getUserName(
                        req.getParameter(ParamsDefinition.SIGNER)
                )
        );
        buildResult(resp);
	}


    private String getUserName(String userId) {
        NodeRef userNodeRef = personService.getPerson(userId);
        String firstName = (String) nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME);
        return firstName + (lastName.isEmpty()? "" : " " + lastName);
    }

    private void buildResult(WebScriptResponse response) throws IOException {
        response.setContentType("application/json");
        response.setContentEncoding("UTF-8");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Pragma","no-cache");
        JSONObject result = new JSONObject();
        try {
            result.put("data", true);
            result.write(response.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public VersionService getVersionService() {
        return versionService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

	private class BusinessException extends Throwable {
		public BusinessException(String s) {
			super(s);
		}
	}
}
