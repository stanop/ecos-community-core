package ru.citeck.ecos.pages;


import ru.citeck.ecos.pages.menu.MenuElements;

public class PageBase {
     private MenuElements menuElements = new MenuElements();
    public MenuElements getMenu()
    {
        return menuElements;
    }
}
