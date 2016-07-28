package ru.citeck.ecos.pages.menu;

import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.LoginPage;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class MenuElements {
    public LoginPage logOut(String userName)
    {
        $(byText(userName)).shouldBe(present).click();
        $("[href = \"/share/page/dologout\"]").shouldBe(present).click();
        LoginPage loginPage = new LoginPage();
        return loginPage;
    }
    public AdminToolsPage openAdminTools()
    {
        AdminToolsPage adminToolsPage = new AdminToolsPage();
        //$("[id *= \"alfresco/header/AlfMenuBarPopup\"]").$(byText("More...")).shouldBe(present).click();
        $(byText("More...")).shouldBe(present).click();
        $(byText("Users")).shouldBe(present).click();
        return adminToolsPage;
    }
    public HomePageSiteContracts goToContracts()
    {
        $("[id *= \"sites-button\"]").shouldBe(present).click();
        $("a[href *= \"site=contracts\"]").shouldBe(present).click();
        HomePageSiteContracts homePageContracts = new HomePageSiteContracts();
        return homePageContracts;
    }
}
