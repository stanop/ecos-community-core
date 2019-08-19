package ru.citeck.ecos.menu;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.cache.sync.SyncKeysService;
import ru.citeck.ecos.config.EcosConfigService;
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

    private static final String MENU_SYNC_KEY = "ecos-menu";

    private static final String DEFAULT_AUTHORITY = "GROUP_EVERYONE";
    private static final String AUTHORITY_ORDER_KEY = "menu-config-authority-order";

    private AuthorityUtils authorityUtils;
    private RepoContentDAOImpl<MenuConfig> registry;
    private MenuFactory factory;

    private PersonService personService;
    private EcosConfigService ecosConfigService;
    private SyncKeysService syncKeysService;

    @Override
    public Menu queryMenu() {
        return buildMenuByUser(AuthenticationUtil.getRunAsUser());
    }

    @Override
    public Menu queryMenu(String userName) {
        if (!personService.personExists(userName)) {
            throw new IllegalArgumentException(String.format("User '%s' does not exist.", userName));
        }
        if (StringUtils.equals(userName, AuthenticationUtil.getRunAsUser())) {
            return buildMenuByUser(userName);
        }
        return AuthenticationUtil.runAs(() -> buildMenuByUser(userName), userName);
    }

    private Menu buildMenuByUser(String userName) {
        List<String> authorities = getOrderedAuthorities(userName);
        return authorities.stream()
                .map(this::queryMenuConfigByAuth)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mc -> factory.getResolvedMenu(mc))
                .findFirst()
                .orElseGet(this::defaultMenu);
    }

    private Menu defaultMenu() {
        MenuConfig menuConfig = queryMenuConfigByAuth(DEFAULT_AUTHORITY)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Can't find default menu config with %s authority.", DEFAULT_AUTHORITY)));
        return factory.getResolvedMenu(menuConfig);
    }

    private Optional<MenuConfig> queryMenuConfigByAuth(String authority) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(MenuConfigModel.PROP_AUTHORITIES, authority);
        return registry.getContentData(keys, true)
                .stream().findFirst()
                .flatMap(ContentData::getData);
    }

    private List<String> getOrderedAuthorities(String userName) {
        Set<String> allUserAuthorities = authorityUtils.getUserAuthorities(userName);
        String defaultOrderParam = StringUtils.defaultString((String)
                ecosConfigService.getParamValue(AUTHORITY_ORDER_KEY));
        List<String> defaultOrder = new ArrayList<>(Arrays.asList(defaultOrderParam.split(",")));
        defaultOrder.retainAll(allUserAuthorities);
        allUserAuthorities.removeAll(defaultOrder);
        allUserAuthorities.remove(userName);
        List<String> orderedAuthorities = new LinkedList<>();
        orderedAuthorities.add(userName);
        orderedAuthorities.addAll(defaultOrder);
        orderedAuthorities.addAll(allUserAuthorities);
        return orderedAuthorities;
    }

    @Override
    public void resetCache() {
        syncKeysService.update(MENU_SYNC_KEY);
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

    @Autowired
    public void setSyncKeysService(SyncKeysService syncKeysService) {
        this.syncKeysService = syncKeysService;
    }

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Autowired
    @Qualifier("ecosConfigService")
    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }
}
