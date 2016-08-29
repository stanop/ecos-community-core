package ru.citeck.ecos.Tests;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.LoginPage;
import ru.citeck.ecos.pages.PageBase;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestForContract.class,
        TestForSupplementaryAgreement.class
})
public class TestSuite extends TestSuiteBase {
    static protected String titleLoginPageRUS = "Citeck EcoS » Войти";
    static protected String titleLoginPageEN = "Citeck EcoS » Login";

    static protected String userName = "Остап";
    static protected String login = "ostap";
    static protected  String pass = "ostap";
    static protected  String group = "company_director";
    static protected String UserNameAdmin = "Administrator";

    @BeforeClass
    static public void createUser()
    {
        createUser(userName, login, pass, group);
    }

    @AfterClass
    static public void deleteUser()
    {
        PageBase pageBase = new PageBase();
        if (pageBase.getMenu().searchByText(userName).exists())
        {
            LoginPage loginPage = pageBase.getMenu().logOut(userName);
            loginPage.inLoginAndPassword(Settings.getLogin(),Settings.getPassword());
            loginPage.clickOnLoginButton();
        }
        AdminToolsPage adminToolsPage = deleteUser(userName);
        LoginPage loginPage = adminToolsPage.getMenu().logOut(UserNameAdmin);
        Assert.assertTrue(titleLoginPageRUS.equals(loginPage.getTitle()) || titleLoginPageEN.equals(loginPage.getTitle()));
    }
}
