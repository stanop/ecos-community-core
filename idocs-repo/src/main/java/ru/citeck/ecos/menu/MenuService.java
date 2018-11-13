package ru.citeck.ecos.menu;

import ru.citeck.ecos.menu.dto.ResolvedMenuConfig;

public interface MenuService {

    ResolvedMenuConfig queryMenuConfig(String userName);

}
