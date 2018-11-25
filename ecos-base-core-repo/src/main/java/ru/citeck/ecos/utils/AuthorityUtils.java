package ru.citeck.ecos.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorityUtils {

    private AuthorityService authorityService;
    private NodeService nodeService;

    public Set<String> getContainedUsers(NodeRef rootRef, boolean immediate) {
        return getContainedUsers(getAuthorityName(rootRef), immediate);
    }

    public Set<String> getContainedUsers(String rootName, boolean immediate) {
        return authorityService.getContainedAuthorities(AuthorityType.USER, rootName, immediate);
    }

    public String getAuthorityName(NodeRef authority) {
        Map<QName, Serializable> properties = nodeService.getProperties(authority);
        String name = (String) properties.get(ContentModel.PROP_AUTHORITY_NAME);
        if (StringUtils.isBlank(name)) {
            name = (String) properties.get(ContentModel.PROP_USERNAME);
        }
        return name;
    }

    public Set<String> getUserAuthorities(String userName) {
        if (StringUtils.isBlank(userName)) {
            return Collections.emptySet();
        }
        Set<String> groups = authorityService.getAuthoritiesForUser(userName);
        Set<String> result = new HashSet<>(groups);
        result.add(userName);
        return result;
    }

    public Set<NodeRef> getNodeRefs(Set<String> authorities) {
        return authorities.stream()
                          .map(authorityService::getAuthorityNodeRef)
                          .collect(Collectors.toSet());
    }

    public NodeRef getNodeRef(String authority) {
        return authorityService.getAuthorityNodeRef(authority);
    }

    public Set<NodeRef> getUserAuthoritiesRefs(String userName) {
        return getNodeRefs(getUserAuthorities(userName));
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

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
