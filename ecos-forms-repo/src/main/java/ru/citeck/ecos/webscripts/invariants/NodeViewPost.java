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
package ru.citeck.ecos.webscripts.invariants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.model.InvariantsModel;
import ru.citeck.ecos.utils.RepoUtils;

public class NodeViewPost extends DeclarativeWebScript {

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_NODEREF = "nodeRef";
    private static final String REQ_VIEW = "view";
    private static final String REQ_VIEW_ID = "id";
    private static final String REQ_VIEW_PARAMS = "params";
    private static final String REQ_ATTRIBUTES = "attributes";
    private static final String REQ_IS_DRAFT = "isDraft";

    private static final String MODEL_NODE = "node";
    
    private NodeViewService nodeViewService;
    private NamespacePrefixResolver prefixResolver;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String typeParam = req.getParameter(PARAM_TYPE);
        String nodeRefParam = req.getParameter(PARAM_NODEREF);

        JSONObject requestBody;
        try {
            Object data = JSONValue.parseWithException(req.getContent().getReader());
            if(!(data instanceof JSONObject)) {
                status.setCode(Status.STATUS_BAD_REQUEST, "Expected JSON object, but got " + data.getClass());
                return null;
            }
            requestBody = (JSONObject) data;
        } catch (IOException e) {
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        } catch (ParseException e) {
            status.setCode(Status.STATUS_BAD_REQUEST, e.getMessage());
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> attributesModel = (Map<String, Object>) requestBody.get(REQ_ATTRIBUTES);
        if(attributesModel == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, REQ_ATTRIBUTES + " should be specified in request body");
            return null;
        }
        Map<QName, Object> attributes = RepoUtils.convertStringMapToQNameMap(attributesModel, prefixResolver);

        Boolean isDraftParam = (Boolean) requestBody.get(REQ_IS_DRAFT);
        if (isDraftParam != null) {
            attributes.put(InvariantsModel.PROP_IS_DRAFT, isDraftParam);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> viewModel = (Map<String, Object>) requestBody.get(REQ_VIEW);
        String viewId = viewModel != null ? (String) viewModel.get(REQ_VIEW_ID) : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> viewParams = viewModel != null ? (Map<String, Object>) viewModel.get(REQ_VIEW_PARAMS) : null;
        
        NodeRef nodeRef;
        if(typeParam != null && !typeParam.isEmpty()) {
            QName type = QName.createQName(typeParam, prefixResolver);
            nodeRef = nodeViewService.saveNodeView(type, viewId, attributes, viewParams);
        } else if(nodeRefParam != null && !nodeRefParam.isEmpty()) {
            if(!NodeRef.isNodeRef(nodeRefParam)) {
                status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_NODEREF + "' should contain nodeRef");
                return null;
            }
            nodeRef = new NodeRef(nodeRefParam);
            nodeViewService.saveNodeView(nodeRef, viewId, attributes, viewParams);
        } else {
            status.setCode(Status.STATUS_BAD_REQUEST, "Either type, or nodeRef parameters should be set");
            return null;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_NODE, nodeRef);
        return model;
    }

    public void setNodeViewService(NodeViewService nodeViewService) {
        this.nodeViewService = nodeViewService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

}
