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
package ru.citeck.ecos.webscripts.document;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionHistory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

public class NameAndLastVersionByNodeRefGet extends BaseAbstractWebscript {

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	protected void executeInternal(WebScriptRequest aRequest,
			WebScriptResponse aResponse) throws Exception {
		// get user name to read tasks for
		String nodeRefParam = aRequest.getParameter("nodeRef");
		JSONObject result = new JSONObject();
		String latestVersion;
		String fileName;
		boolean isError = false;
		String nodeRef = "";

		try {
			VersionHistory versionHistory = serviceRegistry.getVersionService()
					.getVersionHistory(new NodeRef(nodeRefParam));
			if (null != versionHistory) {
				latestVersion = versionHistory.getHeadVersion()
						.getVersionLabel();
				nodeRef = versionHistory.getHeadVersion()
						.getFrozenStateNodeRef().toString();
			} else {
				latestVersion = "";
				isError = true;
			}
		} catch (Exception ex) {
			latestVersion = "";
			isError = true;
		}
		try {
			fileName = (String) serviceRegistry.getNodeService().getProperty(
					new NodeRef(nodeRefParam), ContentModel.PROP_NAME);
		} catch (Exception ex) {
			fileName = "";
			isError = true;
		}
		result.put("lastVersion", latestVersion);
		result.put("fileName", fileName);
		result.put("nodeRef", nodeRef);
		result.put("error", isError);

		// construct resulting JSON
		aResponse.setContentType("application/json");
		aResponse.setContentEncoding("UTF-8");
		aResponse.addHeader("Cache-Control", "no-cache");
		aResponse.addHeader("Pragma", "no-cache");
		// write JSON into response stream
		result.write(aResponse.getWriter());
	}
}
