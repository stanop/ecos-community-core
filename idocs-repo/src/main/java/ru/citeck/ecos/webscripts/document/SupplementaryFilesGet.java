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

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.document.SupplementaryFilesDAO;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

public class SupplementaryFilesGet extends BaseAbstractWebscript {

	private NodeService nodeService;
	private SupplementaryFilesDAO dao;
	
	@Override
	protected void executeInternal(WebScriptRequest aRequest,
			WebScriptResponse aResponse) throws Exception {

		// get user name to read tasks for
		String nodeRefParam = aRequest.getParameter("nodeRef");
		NodeRef nodeRef = new NodeRef(nodeRefParam);

		Collection<NodeRef> files = dao.getSupplementaryFiles(nodeRef);
		
		// construct resulting JSON
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("data", array);

		String targetRefs = "";
		int i = 0;
		
		for (NodeRef ref : files) {
			if (i==files.size()-1) targetRefs = targetRefs + ref;
			else targetRefs = targetRefs + ref+",";
			i++;
		}
		if (files.isEmpty()) {
			JSONObject data = new JSONObject();
			data.put("targetRefs",targetRefs);
			array.put(data);
		}
		for (NodeRef ref : files) {
			JSONObject data = new JSONObject();
			data.put("title",
					nodeService.getProperty(ref, ContentModel.PROP_NAME));
			data.put("nodeRef", ref);
			data.put("targetRefs", targetRefs);
			array.put(data);

		}

		aResponse.setContentType("application/json");
		aResponse.setContentEncoding("UTF-8");
		aResponse.addHeader("Cache-Control", "no-cache");
		aResponse.addHeader("Pragma", "no-cache");
		// write JSON into response stream
		result.write(aResponse.getWriter());
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDao(SupplementaryFilesDAO dao) {
		this.dao = dao;
	}
}
