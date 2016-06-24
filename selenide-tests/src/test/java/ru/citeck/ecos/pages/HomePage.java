package ru.citeck.ecos.pages;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class HomePage {

    public String getTitle()
    {
        return title();
    }

    public void openFormCreateSite()
    {
        $(By.id("HEADER_SITES_MENU_text")).shouldBe(present).click();
        $(By.id("HEADER_SITES_MENU_CREATE_SITE_text")).shouldBe(present).click();
    }

    public SiteHomePage createSite(String mySite)
    {
        SiteHomePage siteHomePage = new SiteHomePage();
        $(By.cssSelector("#alfresco-createSite-instance-form  #alfresco-createSite-instance-title")).shouldBe(present).setValue(mySite);
        $(By.cssSelector("#alfresco-createSite-instance-ok-button > span > button")).shouldBe(present).click();
        //sleep(5000);
        $(By.cssSelector("#HEADER_SITE_DASHBOARD")).shouldBe(present);
        return siteHomePage;
    }

    public HomePage cancelCreateSiteFromHomePage()
    {
        HomePage homePage = new HomePage();
        $(By.cssSelector("#alfresco-createSite-instance-cancel-button > span > button")).shouldBe(present).click();
        return homePage;
    }

    public AdminToolsPage openAdminTools()
    {
        AdminToolsPage adminToolsPage = new AdminToolsPage();
        $("#HEADER_ADMIN_CONSOLE_text > a").shouldBe(present).click();
        //open(Settings.Settings.getBaseURL()+"/console/admin-console/users");
        return adminToolsPage;
    }


}
