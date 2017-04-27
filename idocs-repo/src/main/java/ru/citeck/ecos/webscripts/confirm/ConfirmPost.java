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
package ru.citeck.ecos.webscripts.confirm;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.confirm.ConfirmException;
import ru.citeck.ecos.confirm.ConfirmService;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: alexander.nemerov
 * Date: 24.09.13
 */
public class ConfirmPost extends DeclarativeWebScript {

    // web script arguments
    private static final String PARAM_NODEREF = "nodeRef";
    private static final String PARAM_DECISION = "decision";
    private static final String VERSION_LABEL = "versionLabel";


    private NodeService nodeService;
    private ConfirmService confirmService;

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        String nodeRefStr = req.getParameter(PARAM_NODEREF);
        final String decision = req.getParameter(PARAM_DECISION);
        final String versionLabel = req.getParameter(VERSION_LABEL);

        if (nodeRefStr == null || decision == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter nodeRef and decision is mandatory");
            return null;
        }

        final NodeRef nodeRef = new NodeRef(nodeRefStr);

        if (!nodeService.exists(nodeRef)) {
            status.setCode(Status.STATUS_BAD_REQUEST, "There are no document with NodeRef:" + nodeRef);
            return null;
        }

        try {
            confirmService.setDecision(decision, versionLabel, nodeRef);
        } catch (ConfirmException e) {
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("success", true);

        return model;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setConfirmService(ConfirmService confirmService) {
        this.confirmService = confirmService;
    }
}
