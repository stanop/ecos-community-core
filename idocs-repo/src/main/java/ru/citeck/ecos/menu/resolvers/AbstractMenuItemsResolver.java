package ru.citeck.ecos.menu.resolvers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.menu.MenuService;

import javax.annotation.PostConstruct;

public abstract class AbstractMenuItemsResolver implements MenuItemsResolver {

    private MenuService menuService;

    @PostConstruct
    public void registerResolver() {
        menuService.addResolver(this);
    }

    @Autowired
    @Qualifier("menuService")
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

}
