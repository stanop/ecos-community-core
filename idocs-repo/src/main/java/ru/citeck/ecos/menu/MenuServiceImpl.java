package ru.citeck.ecos.menu;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAOImpl;
import ru.citeck.ecos.menu.dto.MenuConfigFactory;
import ru.citeck.ecos.menu.dto.ResolvedMenuConfig;
import ru.citeck.ecos.menu.xml.MenuConfig;
import ru.citeck.ecos.model.MenuConfigModel;
import ru.citeck.ecos.utils.AuthorityUtils;

import java.io.Serializable;
import java.util.*;

public class MenuServiceImpl implements MenuService {

    private AuthorityUtils authorityUtils;
    private RepoContentDAOImpl<MenuConfig> registry;
    private MenuConfigFactory factory;

    private static final String DEFAULT_AUTHORITY = "GROUP_EVERYONE";

    @Override
    public ResolvedMenuConfig queryMenuConfig(String userName) {
        String userAuthority = getUserAuthority(userName);
        MenuConfig menuConfig = queryMenuConfigByAuth(userAuthority);
        return factory.getResolvedMenuConfig(menuConfig, userName);
    }

    private String getUserAuthority(String userName) {
        Set<String> result = authorityUtils.getUserAuthorities(userName);
//        TODO: which authority is needed? selecting the first one for now
        return result.stream()
                /* authorityUtils adds userName to result set, we don't need it here */
                .filter(auth -> !auth.equals(userName))
                .findFirst().orElse(DEFAULT_AUTHORITY);
    }

    private MenuConfig queryMenuConfigByAuth(String authority) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(MenuConfigModel.PROP_AUTHORITIES, authority);
        return registry.getContentData(keys, true)
                .stream().findFirst()
                .flatMap(ContentData::getData)
                .orElseThrow(() -> new RuntimeException(
                        String.format("MenuConfig with \"%s\" authority is not found",authority)));
    }

    public void setAuthorityUtils(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }

    public void setRegistry(RepoContentDAOImpl<MenuConfig> registry) {
        this.registry = registry;
    }

    public void setFactory(MenuConfigFactory factory) {
        this.factory = factory;
    }

    public RepoContentDAOImpl getRegistry() {
        return registry;
    }

    public MenuConfigFactory getFactory() {
        return factory;
    }
}
