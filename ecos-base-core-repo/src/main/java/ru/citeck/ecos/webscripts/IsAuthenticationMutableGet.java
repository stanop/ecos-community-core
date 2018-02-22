/*
 * Copyright (C) 2008-2018 Citeck LLC.
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

package ru.citeck.ecos.webscripts;

import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Web-script to check whether user's authentication may be mutated
 * via the other methods.
 *
 * @author Andrew Timokhin
 */
public class IsAuthenticationMutableGet extends DeclarativeWebScript {

    private static final String IS_AUTHENTICATION_MUTABLE = "isAuthenticationMutable";

    private MutableAuthenticationService authenticationService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String username = req.getParameter("username");

        if (StringUtils.isBlank(username)) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username must not be empty");
        }

        Map<String, Object> model = new HashMap<>();

        model.put(IS_AUTHENTICATION_MUTABLE, authenticationService.isAuthenticationMutable(username));

        return model;
    }

    /* Setters and Getters */

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}