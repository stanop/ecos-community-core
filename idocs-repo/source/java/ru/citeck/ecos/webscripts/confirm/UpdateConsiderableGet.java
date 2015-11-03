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
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.confirm.ConfirmService;
import ru.citeck.ecos.webscripts.common.BaseAbstractWebscript;

/**
 * Author: alexander.nemerov
 * Date: 04.10.13
 */
public class UpdateConsiderableGet extends BaseAbstractWebscript{

    // web script arguments
    private static final String PARAM_NODEREF = "nodeRef";

    private NodeService nodeService;
    private ConfirmService confirmService;

    @Override
    protected void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws Exception {

        String nodeRefStr = req.getParameter(PARAM_NODEREF);

        if (nodeRefStr == null) {
            throw new Exception("Parameter nodeRef and decision is mandatory");
        }

        final NodeRef nodeRef = new NodeRef(nodeRefStr);

        if (!nodeService.exists(nodeRef)) {
            throw new Exception("There are no document with NodeRef:" + nodeRef);
        }

        confirmService.updateConsiderable(nodeRef);

    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setConfirmService(ConfirmService confirmService) {
        this.confirmService = confirmService;
    }
}
