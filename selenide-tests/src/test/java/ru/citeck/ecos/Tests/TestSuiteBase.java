package ru.citeck.ecos.Tests;

import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.DocumentDetailsPage;
import ru.citeck.ecos.pages.HomePage;

import static com.codeborne.selenide.Condition.present;

public class TestSuiteBase extends SelenideTests{

    static protected void createUser(String username, String login, String password, String group)
    {
        HomePage homePage = new HomePage();
        AdminToolsPage adminToolsPage =  homePage.getMenu().openAdminTools();
        adminToolsPage.openUserContent().shouldBe(present);

        adminToolsPage.clickOnButtonNewUser().shouldBe(present);

        adminToolsPage.selectGroup(group);
        adminToolsPage.setValueOnFromCreateNewUser(username,login,password);
        adminToolsPage.clickOnButtonCreate();

        adminToolsPage.searchUser(login).shouldBe(present);
    }

    static protected AdminToolsPage deleteUser(String username)
    {
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        AdminToolsPage adminToolsPage = detailsPage.getMenu().openAdminTools();
        adminToolsPage.searchUser(username).shouldBe(present);
        adminToolsPage.clickOnUserName(username);
        adminToolsPage.clickOnButtonDeleteUser();
        return adminToolsPage;
    }
}
