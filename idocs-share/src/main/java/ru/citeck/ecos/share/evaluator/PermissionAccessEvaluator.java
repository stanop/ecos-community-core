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
package ru.citeck.ecos.share.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;


public class PermissionAccessEvaluator extends BaseEvaluator {

    public static final String KEY_NODE_REF = "nodeRef";

    public static final String ALFRESCO_ENDPOINT_ID = "";
    public static final String REPO_WEB_SERVICE_URL = "";

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        //String nodeRefId = (String) jsonObject.get(KEY_NODE_REF);
        //jsonObject.get('node').get('permissions').get('user')
        //RequestContext context = ThreadLocalRequestContext.getRequestContext();
        //User user = context.getUser();
        //user.getId()

        final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        final String userId = rc.getUserId();
        final Connector conn;
        try {
            conn = rc.getServiceRegistry().getConnectorService().getConnector(
                    ALFRESCO_ENDPOINT_ID,
                    userId,
                    ServletUtil.getSession()
            );
        } catch (ConnectorServiceException e) {
            throw new RuntimeException(e.toString());
        }

        final Response response = conn.call(REPO_WEB_SERVICE_URL);
        if (response.getStatus().getCode() == Status.STATUS_OK) {
            //do something
        } else {
            //do something else
        }


        return true;
    }

}




