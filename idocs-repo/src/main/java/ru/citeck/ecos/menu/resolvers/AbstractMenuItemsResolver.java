package ru.citeck.ecos.menu.resolvers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.menu.dto.MenuFactory;

import javax.annotation.PostConstruct;

public abstract class AbstractMenuItemsResolver implements MenuItemsResolver {

    private MenuFactory menuFactory;

    @PostConstruct
    public void registerResolver() {
        menuFactory.addResolver(this);
    }

    @Autowired
    @Qualifier("ecos.menu.menuFactory")
    public void setMenuFactory(MenuFactory menuFactory) {
        this.menuFactory = menuFactory;
    }

}
