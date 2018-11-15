package ru.citeck.ecos.menu.resolvers;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.menu.dto.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserSitesResolver implements MenuItemsResolver {

    private static final String ID = "USER_SITES";
    private static final String SITE_NAME_KEY = "siteName";
    private static final String SITE_LINK_KEY = "SITE_LINK";
    private SiteService siteService;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        return getUserSites().stream()
                .map(this::constructItem)
                .collect(Collectors.toList());
    }

    private Element constructItem(SiteInfo site) {
        String name = site.getShortName();
        Element element = new Element();
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(SITE_NAME_KEY, name);
        element.setLabel(site.getTitle());
        element.setContextId(name);
        element.setId(name);
        element.setAction(SITE_LINK_KEY, actionParams);
        return element;
    }

    private List<SiteInfo> getUserSites() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (StringUtils.isNotBlank(userName)) {
            return siteService.listSites(userName);
        }
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Autowired
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
