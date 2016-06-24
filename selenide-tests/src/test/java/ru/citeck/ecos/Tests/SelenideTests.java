package ru.citeck.ecos.Tests;

import ru.citeck.ecos.Settings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.citeck.ecos.pages.LoginPage;
import com.codeborne.selenide.Configuration;

import static com.codeborne.selenide.Selenide.close;

public class SelenideTests {
    @BeforeClass
    public static void Login()
    {
        Configuration.timeout = 10000;
        LoginPage loginPage = new LoginPage();
        loginPage.inLoginAndPassword(Settings.getLogin(), Settings.getPassword());
        loginPage.pressEnter();
    }

    @AfterClass
    public static void closeBrowser()
    {
        close();
    }
}

