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
package ru.citeck.ecos.webscripts.authority;

import java.io.IOException;
import java.util.Set;

import org.alfresco.service.cmr.security.AuthorityService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AlfrescoAuthorityFinder extends AbstractWebScript {

	private AuthorityService authorityService;
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		
		String userName = req.getServiceMatch().getTemplateVars().get("username");
		String accessGroup = req.getServiceMatch().getTemplateVars().get("accessgroup");
		
		Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(userName);
		
		JSONObject result = new JSONObject();
		
		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		res.addHeader("Cache-Control", "no-cache");
		res.addHeader("Pragma","no-cache");

		try {
			result.put("data", authoritiesForUser.contains(accessGroup));
			result.write(res.getWriter());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
