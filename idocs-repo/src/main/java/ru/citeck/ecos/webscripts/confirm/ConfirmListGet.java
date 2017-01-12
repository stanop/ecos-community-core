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
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.confirm.ConfirmListService;

import java.util.Map;

public class ConfirmListGet extends DeclarativeWebScript
{

	// web script arguments
    private static final String PARAM_NODEREF = "nodeRef";

    private ConfirmListService confirmList;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {

        String nodeRefStr = req.getParameter(PARAM_NODEREF);


        if(nodeRefStr == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter nodeRef is mandatory");
            return null;
        }

        NodeRef document = new NodeRef(nodeRefStr);

        Map<String, Object> result;
        try {
            result = confirmList.getModel(document);
        } catch (Exception e) {
            status.setCode(Status.STATUS_BAD_REQUEST, e.getMessage());
            result = null;
        }
        return result;
    	
    }

    public void setConfirmList(ConfirmListService confirmList) {
        this.confirmList = confirmList;
    }
}
