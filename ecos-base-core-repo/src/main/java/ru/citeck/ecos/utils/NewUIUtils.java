package ru.citeck.ecos.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NewUIUtils {

    private static final String NEW_UI_REDIRECT_ENABLED = "new-ui-redirect-enabled";
    private static final String NEW_UI_REDIRECT_URL = "new-ui-redirect-url";
    private static final String V2_DASHBOARD_URL_DEFAULT = "/v2/dashboard";
    private static final String DEFAULT_UI_NEW_JOURNALS_ACCESS_GROUPS = "default-ui-new-journals-access-groups";

    private static final String FORCE_OLD_CARD_DETAILS_ATT = "_etype.attributes.forceOldCardDetails?bool";

    private final EcosConfigService ecosConfigService;
    private final AuthenticationService authenticationService;
    private final AuthorityService authorityService;
    private final RecordsService recordsService;

    private LoadingCache<RecordRef, Boolean> oldCardDetailsCache;
    private LoadingCache<String, Boolean> isNewUIEnabledCache;

    @Autowired
    public NewUIUtils(@Qualifier("ecosConfigService") EcosConfigService ecosConfigService,
                      @Qualifier("authenticationService") AuthenticationService authenticationService,
                      AuthorityService authorityService,
                      RecordsService recordsService) {

        this.ecosConfigService = ecosConfigService;
        this.authenticationService = authenticationService;
        this.authorityService = authorityService;
        this.recordsService = recordsService;

        isNewUIEnabledCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(CacheLoader.from(this::isNewUIEnabledForUserImpl));

        oldCardDetailsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(CacheLoader.from(this::isOldCardDetailsRequiredImpl));
    }

    public boolean isNewUIEnabled() {
        return isNewUIEnabledForUser(authenticationService.getCurrentUserName());
    }

    public boolean isNewUIEnabledForUser(String username) {
        return isNewUIEnabledCache.getUnchecked(username);
    }

    public String getNewUIRedirectUrl() {
        Object objValue = ecosConfigService.getParamValue(NEW_UI_REDIRECT_URL);
        String newUIRedirectUrl = String.valueOf(objValue);
        if (StringUtils.isBlank(newUIRedirectUrl) || newUIRedirectUrl.equals("null")) {
            newUIRedirectUrl = V2_DASHBOARD_URL_DEFAULT;
        }
        return newUIRedirectUrl;
    }

    public boolean isOldCardDetailsRequired(RecordRef recordRef) {
        try {
            return oldCardDetailsCache.getUnchecked(recordRef);
        } catch (Exception e) {
            log.error("Exception. RecordRef: " + recordRef, e);
            return false;
        }
    }

    private boolean isNewUIEnabledForUserImpl(String username) {
        Object objValue = ecosConfigService.getParamValue(NEW_UI_REDIRECT_ENABLED);
        boolean isNewUIRedirectEnabled = String.valueOf(objValue).equals(Boolean.TRUE.toString());
        return isNewUIRedirectEnabled || isNewJournalsGroupMember(username);
    }

    private boolean isOldCardDetailsRequiredImpl(RecordRef recordRef) {
        if (!isNewUIEnabled()) {
            return true;
        }
        JsonNode res = recordsService.getAttribute(recordRef, FORCE_OLD_CARD_DETAILS_ATT);
        return BooleanNode.TRUE.equals(res);
    }

    private boolean isNewJournalsGroupMember(String username) {
        String groupsInString = (String) ecosConfigService.getParamValue(DEFAULT_UI_NEW_JOURNALS_ACCESS_GROUPS);
        if (StringUtils.isEmpty(groupsInString)) {
            return false;
        }
        Set<String> avalibleGroups = new HashSet<>(Arrays.asList(groupsInString.split(",")));
        Set<String> userGroups = authorityService.getAuthoritiesForUser(username);
        return !Collections.disjoint(avalibleGroups, userGroups);
    }
}
