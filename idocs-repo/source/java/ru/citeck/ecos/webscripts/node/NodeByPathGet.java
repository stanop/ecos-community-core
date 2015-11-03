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
package ru.citeck.ecos.webscripts.node;

import org.alfresco.repo.jscript.Search;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: alexander.nemerov
 * Date: 23.11.13
 */
public class NodeByPathGet extends DeclarativeWebScript {

	// web script arguments
	private static final String PARAM_PATH = "path";

	private SearchService searchService;

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String path = req.getParameter(PARAM_PATH);
		if(path == null) {
			status.setCode(Status.STATUS_BAD_REQUEST, "Parameter path is mandatory");
			return null;
		}
		String newPath = "";
		Search search = new Search();
		String[] parts = path.split("/");
		for(int i = 1; i < parts.length; i++) {
			String[] prefixParts = parts[i].split(":");
			newPath += "/" + prefixParts[0] + ":" + search.ISO9075Encode(prefixParts[1]);
		}
		String query = "PATH:\"" + newPath + "\"";
		ResultSet nodes = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				SearchService.LANGUAGE_LUCENE, query);
		if(nodes.length() > 1) {
			status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR, "Nodes length > 1: " + nodes.length());
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		if(nodes.length() == 0) {
			result.put("found", false);
			return result;
		}
		result.put("found", true);
		result.put("nodeRef", nodes.getNodeRef(0).toString());
		return result;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
