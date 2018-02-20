package ru.citeck.ecos.webscripts;

import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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