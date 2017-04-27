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
package ru.citeck.ecos.webscripts.documentregistration;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.counter.EnumerationException;
import ru.citeck.ecos.counter.EnumerationService;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.service.CiteckServices;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Alexander Nemerov
 * @date: 28.01.14
 */
public class RegisterPost extends DeclarativeWebScript {

    private static final String PARAM_NODEREF = "nodeRef";

    private NodeService nodeService;
    private EnumerationService enumerationService;
    private NodeInfoFactory nodeInfoFactory;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeRefStr = req.getParameter(PARAM_NODEREF);
        if(nodeRefStr == null) {
            throw new RuntimeException("nodeRef is required");
        }
        final NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                nodeService.setProperty(nodeRef, IdocsModel.PROP_REGISTRATION_DATE, new Date());
                nodeService.setProperty(nodeRef, IdocsModel.PROP_DOCUMENT_STATUS, "onConsideration");

                NodeRef template = enumerationService.getTemplate(getTemplateName(nodeRef));
                NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo(nodeRef);

                String number = null;
                try {
                    number = enumerationService.getNumber(template, nodeInfo);
                } catch (EnumerationException e) {
                    throw new AlfrescoRuntimeException(e.getMessage(), e);
                }

                nodeService.setProperty(nodeRef, IdocsModel.PROP_REGISTRATION_NUMBER, number);
                return null;
            }
        });

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("nodeRef", nodeRef);
        return result;
    }

    //ugly solution
    private String getTemplateName(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        if(type.isMatch(IdocsModel.TYPE_ATTORNEY)) {
            return "idocs-attorney-number-template";
        } else {
            return null;
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.enumerationService = (EnumerationService) serviceRegistry.getService(CiteckServices.ENUMERATION_SERVICE);
    }

    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }
}
