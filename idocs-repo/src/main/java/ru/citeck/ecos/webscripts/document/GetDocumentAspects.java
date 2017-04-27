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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

import java.util.Set;

public class GetDocumentAspects extends BaseAbstractWebscript {

	private NodeService nodeService;

	public static interface ParamsDefinition {
		public static String NODE_REF = "nodeRef";
	}

	@Override
	protected void executeInternal(WebScriptRequest req, WebScriptResponse resp)
			throws Exception {
		String sRefParam = req.getParameter(ParamsDefinition.NODE_REF);
		NodeRef nodeRef = new NodeRef(sRefParam);
		Set<QName> aspects = nodeService.getAspects(nodeRef);
		buildResult(resp, aspects);
	}

	private void buildResult(WebScriptResponse resp, Set<QName> aspects)
			throws Exception {
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("data", array);
		for(QName aspect : aspects) {
			array.put(aspect.getLocalName());
		}
		resp.setContentType("application/json");
		resp.setContentEncoding("UTF-8");
		resp.addHeader("Cache-Control", "no-cache");
		resp.addHeader("Pragma", "no-cache");
		// write JSON into response stream
		result.write(resp.getWriter());
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
