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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


public class ValidCheck extends AbstractWebScript {

	private NodeService nodeService;

    public static interface ParamsDefinition {
        public static String NODE_REF = "nodeRef";
        public static String SIGNATURE_VALUE = "sign";
        public static String DATA = "data";
    }

	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		String sNodeRef = req.getParameter(ParamsDefinition.NODE_REF);
		String sign = req.getParameter(ParamsDefinition.SIGNATURE_VALUE);
		String data = req.getParameter(ParamsDefinition.DATA);

		String[] dataArr = data.split(",");
		byte[] b = new byte[dataArr.length];
		for (int i= 0; i < dataArr.length; i++){
		    b[i] = Byte.decode(dataArr[i]);
		}
		boolean flag = false;
		if (!sign.isEmpty()) {
            SignatureUtil instance = SignatureUtil.getInstance();
            List<String> resultVerify = new ArrayList<String>();
            try {
                resultVerify = instance.verifySignature(sign.getBytes(), b);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!resultVerify.isEmpty()) {
                flag = true;
            }
        }
        buildResult(resp, flag);
	}

    private void buildResult(WebScriptResponse resp, boolean flag) throws IOException {
        resp.setContentType("application/json");
        resp.setContentEncoding("UTF-8");
        resp.addHeader("Cache-Control", "no-cache");
        resp.addHeader("Pragma", "no-cache");
        JSONObject result = new JSONObject();
        try {
            result.put("data", flag);
            result.write(resp.getWriter());
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
}
