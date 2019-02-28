package ru.citeck.ecos.menu;

import ru.citeck.ecos.menu.dto.Menu;

public interface MenuService {

    Menu queryMenu();

    Menu queryMenu(String userName);

    void resetCache();
}
