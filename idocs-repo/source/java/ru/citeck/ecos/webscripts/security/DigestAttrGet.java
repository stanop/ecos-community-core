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
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


public class DigestAttrGet extends BaseAbstractWebscript {

    private ContentService contentService;
    private NodeService nodeService;
    private VersionService versionService;


    public static interface ParamsDefinition {
        public static String NODE_REF = "nodeRef";
//        public static String LAST_VERSION_NODE_REF = "lastVerNodeRef";
    }

    @Override
	protected void executeInternal(WebScriptRequest req, WebScriptResponse resp)
            throws Exception {
		String sNodeRef = req.getParameter(ParamsDefinition.NODE_REF);
		NodeRef nodeRef = new NodeRef(sNodeRef);
		if (sNodeRef.contains("versionStore")) {
            nodeRef = VersionUtil.convertNodeRef(nodeRef);
//            if (isHeadVersion(nodeRef)) {
//                nodeRef = new NodeRef(
//                        req.getParameter(ParamsDefinition.LAST_VERSION_NODE_REF)
//                );
//            }
        }
        buildResult(resp, getNodeContent(nodeRef), getNodePropAndAsocValues(nodeRef));
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

    private String getNodePropAndAsocValues(final NodeRef nodeRef) {
        Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
        Set<Entry<QName,Serializable>> setProps = props.entrySet();
        List<String> values = new LinkedList<String>();
	    for (Entry prop : setProps) {
            QName key = (QName) prop.getKey();
            if (isValidProp(key)) {
                String value = prop.getValue() != null ? prop.getValue().toString() : "";
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }
	    Collections.sort(values);
        return values.toString();
    }

    /**
     * Check if specified property should be signed.
     * Some properties should not be signed, including signature itself, 
     *  and system properties, that can change accidentally.
     */
    private boolean isValidProp(QName prop) {
        //not valid props
        if (prop.equals(SecurityModel.PROP_SIGNATURE_VALUE) || prop.equals(SecurityModel.PROP_SIGNER)) {
            return false;
        }
        //not valid namespace
        if (NamespaceService.CONTENT_MODEL_1_0_URI.equals(prop.getNamespaceURI()) ||
                NamespaceService.SYSTEM_MODEL_1_0_URI.equals(prop.getNamespaceURI()) ||
                NamespaceService.EXIF_MODEL_1_0_URI.equals(prop.getNamespaceURI()) ||
		        Version2Model.NAMESPACE_URI.equals(prop.getNamespaceURI())) {
            //exceptions
            if (ContentModel.PROP_NAME.equals(prop) ||
		            ContentModel.PROP_CATEGORIES.equals(prop) ||
		            ContentModel.PROP_DESCRIPTION.equals(prop)) {
                return true;
            }
            return false;
        }
        return true;
    }

    private OutputStream getNodeContent(NodeRef nodeRef) {
        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        OutputStream out = new ByteArrayOutputStream();
        if(contentReader != null) {
        	contentReader.getContent(out);
        }
        return out;
    }

    private void buildResult(WebScriptResponse resp, OutputStream content, String values) throws Exception {
        //result string with digest + properties
        SignatureUtil signUtil = SignatureUtil.getInstance();
        String resultString = content.toString();
        byte[] digest = signUtil.calculateDigest(resultString.getBytes(), signUtil.DIGEST_ALG_NAME);
        resultString = new String(digest) + values;
        byte[] asd = resultString.getBytes();
        String base64Array = Base64.encodeBytes(resultString.getBytes());
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("data", array);
        JSONObject data = new JSONObject();
        data.put("base64",base64Array);
        data.put("result",resultString.getBytes());
        array.put(data);
        resp.setContentType("application/json");
        resp.setContentEncoding("UTF-8");
        resp.addHeader("Cache-Control", "no-cache");
        resp.addHeader("Pragma", "no-cache");
        // write JSON into response stream
        result.write(resp.getWriter());
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
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
