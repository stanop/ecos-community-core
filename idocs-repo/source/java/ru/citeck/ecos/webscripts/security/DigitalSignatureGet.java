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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

import java.io.IOException;

public class DigitalSignatureGet extends BaseAbstractWebscript {

	private NodeService nodeService;
    private VersionService versionService;

    public static interface ParamsDefinition {
        public static String NODE_REF = "nodeRef";
//        public static String LAST_VERSION_NODE_REF = "lastVerNodeRef";
    }

    @Override
	protected void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws Exception {
		String sRefParam = req.getParameter(ParamsDefinition.NODE_REF);
		NodeRef nodeRef = new NodeRef(sRefParam);
		if (sRefParam.contains("versionStore")) {
            nodeRef = VersionUtil.convertNodeRef(nodeRef);
//            if (isHeadVersion(nodeRef)) {
//                nodeRef = new NodeRef(
//                        req.getParameter(ParamsDefinition.LAST_VERSION_NODE_REF)
//                );
//            }
        }
        buildResult(
                resp,
                (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_SIGNATURE_VALUE),
                (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_SIGNER)
        );
	}

//    private boolean isHeadVersion(final NodeRef nodeRef) {
//        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
//            @Override
//            public Boolean doWork() throws Exception {
//                Version headVersion = versionService.getVersionHistory(nodeRef).getHeadVersion();
//                return headVersion.getFrozenStateNodeRef().equals(nodeRef);
//            }
//        });
//    }

    private void buildResult(WebScriptResponse resp, String sign, String signer) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("data", array);
        if (sign != null && signer != null){
            JSONObject data = new JSONObject();
            data.put("sign", sign);
            data.put("signer",signer);
            array.put(data);
        }else {
            JSONObject data = new JSONObject();
            data.put("sign", "");
            data.put("signer", "");
            array.put(data);
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

    public VersionService getVersionService() {
        return versionService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }
}
