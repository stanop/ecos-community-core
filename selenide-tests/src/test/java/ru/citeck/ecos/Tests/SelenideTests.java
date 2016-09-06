package ru.citeck.ecos.Tests;

import org.junit.Rule;
import ru.citeck.ecos.Settings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.citeck.ecos.pages.LoginPage;
import com.codeborne.selenide.junit.ScreenShooter;

import static com.codeborne.selenide.Selenide.close;

public class SelenideTests {

    @BeforeClass
    public static void login() {
        LoginPage loginPage = new LoginPage();
        loginPage.inLoginAndPassword(Settings.getLogin(), Settings.getPassword());
        loginPage.clickOnLoginButton();
    }

    @Rule
    public ScreenShooter makeScreenshotOnFailure = ScreenShooter.failedTests();

    @AfterClass
    public static void closeBrowser() {
        close();
    }

}

