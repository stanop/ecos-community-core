package ru.citeck.ecos.utils;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorityUtils {

    private AuthorityService authorityService;

    public Set<String> getUserAuthorities(String userName) {
        if (StringUtils.isBlank(userName)) {
            return Collections.emptySet();
        }
        Set<String> groups = authorityService.getAuthoritiesForUser(userName);
        Set<String> result = new HashSet<>(groups);
        result.add(userName);
        return result;
    }

    public Set<NodeRef> getUserAuthoritiesRefs(String userName) {
        return getUserAuthorities(userName).stream()
                                           .map(authorityService::getAuthorityNodeRef)
                                           .collect(Collectors.toSet());
    }

    public Set<String> getUserAuthorities() {
        return getUserAuthorities(AuthenticationUtil.getRunAsUser());
    }

    public Set<NodeRef> getUserAuthoritiesRefs() {
        return getUserAuthoritiesRefs(AuthenticationUtil.getRunAsUser());
    }

    @Autowired
    @Qualifier("authorityService")
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
