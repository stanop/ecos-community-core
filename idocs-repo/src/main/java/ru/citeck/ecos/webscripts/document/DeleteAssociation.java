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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.document.SupplementaryFilesDAO;

public class DeleteAssociation extends AbstractWebScript {

	private SupplementaryFilesDAO dao;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		NodeRef aggNodeRef = new NodeRef(req.getParameter("nodeRef"));
		String[] filesNodeRefs =  req.getParameter("filesNodeRefs").split(",");
		List<NodeRef> files = new ArrayList<NodeRef>(filesNodeRefs.length);
		for(String file : filesNodeRefs) {
			files.add(new NodeRef(file));
		}

		dao.removeSupplementaryFiles(aggNodeRef, files);
		
		try {
			JSONObject result = new JSONObject();
			JSONArray array = new JSONArray();
			result.put("data", array);
			JSONObject data = new JSONObject();
			data.put("resp", "OK");
			array.put(data);
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.addHeader("Cache-Control", "no-cache");
			res.addHeader("Pragma","no-cache");
			result.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Caught exception", e);
		}
		
	}

	public void setDao(SupplementaryFilesDAO dao) {
		this.dao = dao;
	}

}
