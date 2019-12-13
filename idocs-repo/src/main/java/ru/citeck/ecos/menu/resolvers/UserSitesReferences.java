package ru.citeck.ecos.menu.resolvers;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.menu.dto.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Site resolver with site references link instead of site link
 */
@Component
public class UserSitesReferences extends AbstractMenuItemsResolver {

    private static final String ID = "USER_SITES_REFERENCES";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";
    private static final String SITE_NAME_KEY = "siteName";
    private static final String LIST_ID_KEY = "listId";
    private static final String DEFAULT_JOURNAL_LIST_ID = "references";
    private SiteService siteService;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        return getUserSites().stream()
                .map(siteInfo -> constructItem(siteInfo, context, params))
                .collect(Collectors.toList());
    }

    private Element constructItem(SiteInfo site, Element context, Map<String, String> params) {

        String listId = getParam(params, context, LIST_ID_KEY);
        boolean displayIcon = context.getParams().containsKey("rootElement");
        if (StringUtils.isNotEmpty(listId)) {
            params.put(LIST_ID_KEY, listId);
        } else {
            params.put(LIST_ID_KEY, DEFAULT_JOURNAL_LIST_ID);
            listId = DEFAULT_JOURNAL_LIST_ID;
        }

        String siteName = site.getShortName();
        String parentElemId = StringUtils.defaultString(context.getId());
        Element element = new Element();
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(SITE_NAME_KEY, siteName);
        actionParams.put(LIST_ID_KEY, listId);
        element.setLabel(site.getTitle());
        Map<String, String> elementParams = new HashMap<>();
        elementParams.put(SITE_ID_KEY, siteName);
        elementParams.put(LIST_ID_KEY, listId);
        element.setParams(elementParams);
        element.setId(String.format("%s_%s", parentElemId, siteName.toUpperCase()));
        element.setAction(JOURNAL_LINK_KEY, actionParams);
        if (displayIcon) {
            element.setIcon(siteName);
        }
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
