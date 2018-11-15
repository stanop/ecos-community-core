package ru.citeck.ecos.menu;

import ru.citeck.ecos.menu.dto.Menu;
import ru.citeck.ecos.menu.resolvers.MenuItemsResolver;

import java.util.Map;

public interface MenuService {

    Menu queryMenu();

    Menu queryMenu(String userName);

    void addResolver(MenuItemsResolver menuItemsResolver);

    Map<String, MenuItemsResolver> getResolvers();

}
