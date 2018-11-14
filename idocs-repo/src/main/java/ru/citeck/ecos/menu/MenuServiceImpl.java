package ru.citeck.ecos.menu;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAOImpl;
import ru.citeck.ecos.menu.dto.ResolvedMenuConfigFactory;
import ru.citeck.ecos.menu.dto.ResolvedMenuConfig;
import ru.citeck.ecos.menu.xml.MenuConfig;
import ru.citeck.ecos.model.MenuConfigModel;
import ru.citeck.ecos.utils.AuthorityUtils;

import java.io.Serializable;
import java.util.*;

public class MenuServiceImpl implements MenuService {

    private AuthorityUtils authorityUtils;
    private RepoContentDAOImpl<MenuConfig> registry;
    private ResolvedMenuConfigFactory factory;

    private static final String DEFAULT_AUTHORITY = "GROUP_EVERYONE";
    private static final String ADMIN_AUTHORITY = "GROUP_ALFRESCO_ADMINISTRATORS";


    @Override
    public ResolvedMenuConfig queryMenuConfig(String userName) {
        String userAuthority = getUserAuthority(userName);
        MenuConfig menuConfig = queryMenuConfigByAuth(userAuthority);
        return factory.getResolvedMenuConfig(menuConfig, userName);
    }

    private String getUserAuthority(String userName) {
        Set<String> result = authorityUtils.getUserAuthorities(userName);
        return result.contains(ADMIN_AUTHORITY) ? ADMIN_AUTHORITY : DEFAULT_AUTHORITY;
//        TODO: which authority is needed? selecting admin or default for now
    }

    private MenuConfig queryMenuConfigByAuth(String authority) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(MenuConfigModel.PROP_AUTHORITIES, authority);
        return registry.getContentData(keys, true)
                .stream().findFirst()
                .flatMap(ContentData::getData)
                .orElseThrow(() -> new RuntimeException(
                        String.format("MenuConfig with %s authority is not found", authority)));
    }

    public void setAuthorityUtils(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }

    public void setRegistry(RepoContentDAOImpl<MenuConfig> registry) {
        this.registry = registry;
    }

    public void setFactory(ResolvedMenuConfigFactory factory) {
        this.factory = factory;
    }

    public RepoContentDAOImpl getRegistry() {
        return registry;
    }

    public ResolvedMenuConfigFactory getFactory() {
        return factory;
    }
}
