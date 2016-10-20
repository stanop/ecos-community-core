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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.security.AttributesPermissionService;
import ru.citeck.ecos.webscripts.utils.WebScriptUtils;

import java.util.HashMap;
import java.util.Map;

public class NodeViewGet extends DeclarativeWebScript {

    private static final Logger logger = Logger.getLogger(NodeViewGet.class);

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_VIEW_ID = "viewId";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_NODEREF = "nodeRef";
    private static final String MODEL_VIEW = "view";
    private static final String TEMPLATE_PARAM_PREFIX = "param_";
    
    private NodeService nodeService;
    private NodeViewService nodeViewService;
    private NamespacePrefixResolver prefixResolver;
    private AttributesPermissionService attributesPermissionService;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String typeParam = req.getParameter(PARAM_TYPE);
        String viewId = req.getParameter(PARAM_VIEW_ID);
        String mode = req.getParameter(PARAM_MODE);
        String nodeRefParam = req.getParameter(PARAM_NODEREF);
        NodeRef nodeRef = null;

        NodeView.Builder builder = new NodeView.Builder(prefixResolver);
        
        if(typeParam != null && !typeParam.isEmpty()) {
            builder.className(typeParam);
        } else if(nodeRefParam != null && !nodeRefParam.isEmpty()) {
            if(!NodeRef.isNodeRef(nodeRefParam)) {
                status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_NODEREF + "' should contain nodeRef");
                return null;
            }
            nodeRef = new NodeRef(nodeRefParam);
            if(!nodeService.exists(nodeRef)) {
                status.setCode(Status.STATUS_NOT_FOUND, "Node " + nodeRefParam + " does not exist");
                return null;
            }
            builder.className(nodeService.getType(nodeRef));
        } else {
            status.setCode(Status.STATUS_BAD_REQUEST, "Either type, or nodeRef parameters should be set");
            return null;
        }
        
        if(viewId != null) builder.id(viewId);
        if(mode != null) builder.mode(mode);
        
        builder.templateParams(getTemplateParams(req));
        
        NodeView query = builder.build();
        
        if(!nodeViewService.hasNodeView(query)) {
            status.setCode(Status.STATUS_NOT_FOUND, "This view is not registered");
            return null;
        }

        NodeView view = nodeViewService.getNodeView(query);

        if (attributesPermissionService != null) {
            attributesPermissionService.processNodeView(nodeRef, view);
        } else {
            logger.warn("AttributesPermissionService is null");
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_VIEW, view);
        return model;
    }

    private Map<String, Object> getTemplateParams(WebScriptRequest req) {
        Map<String, String> requestParams = WebScriptUtils.getParameterMap(req);
        Map<String, Object> templateParams = new HashMap<>(requestParams.size());
        for(String key : requestParams.keySet()) {
            if(key.startsWith(TEMPLATE_PARAM_PREFIX)) {
                templateParams.put(key.replaceFirst(TEMPLATE_PARAM_PREFIX, ""), requestParams.get(key));
            }
        }
        return templateParams;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNodeViewService(NodeViewService nodeViewService) {
        this.nodeViewService = nodeViewService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    public void setAttributesPermissionService(AttributesPermissionService attributesPermissionService) {
        this.attributesPermissionService = attributesPermissionService;
    }
}
