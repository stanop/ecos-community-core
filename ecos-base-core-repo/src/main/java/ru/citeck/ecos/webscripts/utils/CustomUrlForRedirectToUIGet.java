package ru.citeck.ecos.webscripts.utils;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.config.EcosConfigService;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class CustomUrlForRedirectToUIGet extends AbstractWebScript {

    private static final String NEW_UI_REDIRECT_ENABLED = "new-ui-redirect-enabled";
    private static final String DEFAULT_UI_NEW_JOURNALS_ACCESS_GROUPS = "default-ui-new-journals-access-groups";

    private static final String V2_DASHBOARD_URL = "/v2/dashboard";
    private static final String SHARE_PAGE_URL = "/share/page";
    private static final String RESPONSE_TEMPLATE = "{\"url\":\"%s\"}";

    private EcosConfigService ecosConfigService;
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Object objValue = ecosConfigService.getParamValue(NEW_UI_REDIRECT_ENABLED);

        String username = authenticationService.getCurrentUserName();
        boolean isNewUIRedirectEnabled = String.valueOf(objValue).equals(Boolean.TRUE.toString());
        boolean userContainsInGroups = checkUserInGroupsFromConfig(username);

        String url;
        if (isNewUIRedirectEnabled || userContainsInGroups) {
            url = V2_DASHBOARD_URL;
        } else {
            url = SHARE_PAGE_URL;
        }

        try (Writer out = res.getWriter()) {
            out.write(String.format(RESPONSE_TEMPLATE, url));
            res.setStatus(Status.STATUS_OK);
        }
    }

    private boolean checkUserInGroupsFromConfig(String username) {
        String groupsInString = (String) ecosConfigService.getParamValue(DEFAULT_UI_NEW_JOURNALS_ACCESS_GROUPS);
        if (StringUtils.isEmpty(groupsInString)) {
            return false;
        }

        Set<String> avalibleGroups = new HashSet<>(Arrays.asList(groupsInString.split(",")));
        Set<String> userGroups = getUserGroups(username);
        return !Collections.disjoint(avalibleGroups, userGroups);
    }

    private Set<String> getUserGroups(String username) {
        return authorityService.getAuthoritiesForUser(username);
    }

    @Autowired
    @Qualifier("ecosConfigService")
    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }

    @Autowired
    @Qualifier("authenticationService")
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Autowired
    @Qualifier("authorityService")
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
