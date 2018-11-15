package ru.citeck.ecos.menu;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAOImpl;
import ru.citeck.ecos.menu.dto.MenuFactory;
import ru.citeck.ecos.menu.dto.Menu;
import ru.citeck.ecos.menu.xml.MenuConfig;
import ru.citeck.ecos.model.MenuConfigModel;
import ru.citeck.ecos.utils.AuthorityUtils;

import java.io.Serializable;
import java.util.*;

public class MenuServiceImpl implements MenuService {

    private AuthorityUtils authorityUtils;
    private RepoContentDAOImpl<MenuConfig> registry;
    private MenuFactory factory;

    private static final String DEFAULT_AUTHORITY = "GROUP_EVERYONE";

    @Override
    public Menu queryMenuConfig(String userName) {
//        TODO: this authorities set is not sorted by priority
        Set<String> authorities = authorityUtils.getUserAuthorities(userName);

        return authorities.stream()
                .map(this::queryMenuConfigByAuth)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mc -> factory.getResolvedMenuConfig(mc))
                .findFirst()
                .orElse(defaultMenu());
    }

    private Menu defaultMenu() {
        MenuConfig menuConfig = queryMenuConfigByAuth(DEFAULT_AUTHORITY)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Can't find default menu config with %s authority", DEFAULT_AUTHORITY)));
        return factory.getResolvedMenuConfig(menuConfig);
    }

    private Optional<MenuConfig> queryMenuConfigByAuth(String authority) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(MenuConfigModel.PROP_AUTHORITIES, authority);
        return registry.getContentData(keys, true)
                .stream().findFirst()
                .flatMap(ContentData::getData);
    }

    public void setAuthorityUtils(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }

    public void setRegistry(RepoContentDAOImpl<MenuConfig> registry) {
        this.registry = registry;
    }

    public void setFactory(MenuFactory factory) {
        this.factory = factory;
    }

    public RepoContentDAOImpl getRegistry() {
        return registry;
    }

    public MenuFactory getFactory() {
        return factory;
    }
}
