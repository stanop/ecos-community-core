package ru.citeck.ecos.pages;

import static com.codeborne.selenide.Selenide.title;

public class SiteHomePage {
    public String getTitle()
    {
        return title();
    }
}
