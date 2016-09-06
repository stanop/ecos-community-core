package ru.citeck.ecos.pages.menu;

import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.LoginPage;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class MenuElements {

    public LoginPage logOut() {
        $(".right-widgets span[id*=\"alfresco/header/AlfMenuBarPopup\"]").shouldBe(present).shouldBe(enabled).click();
        $("[href = \"/share/page/dologout\"]").shouldBe(present).shouldBe(enabled).click();
        LoginPage loginPage = new LoginPage();
        return loginPage;
    }

    public AdminToolsPage openAdminTools() {
        AdminToolsPage adminToolsPage = new AdminToolsPage();
        $(byText("More...")).shouldBe(present).shouldBe(enabled).click();
        $(byText("Users")).shouldBe(present).click();
        return adminToolsPage;
    }

    public HomePageSiteContracts goToContracts() {
        $("[id *= \"sites-button\"]").shouldBe(present).shouldBe(enabled).click();
        $("a[href *= \"site=contracts\"]").shouldBe(present).shouldBe(enabled).click();
        HomePageSiteContracts homePageContracts = new HomePageSiteContracts();
        return homePageContracts;
    }
}
