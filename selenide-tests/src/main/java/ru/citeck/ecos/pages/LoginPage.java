package ru.citeck.ecos.pages;


import com.codeborne.selenide.Selenide;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.focused;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;

public class LoginPage {

    public LoginPage()
    {
        Selenide.open("");

    }
    public String getTitle()
    {
        return title();
    }
    public void inLoginAndPassword(String username, String password) {
        $(By.name("username")).shouldBe(focused).setValue(username);
        $(By.name("password")).shouldBe(present).setValue(password);
    }
    public HomePage clickOnLoginButton()
    {
        $(By.tagName("button")).click();
        HomePage homePage = new HomePage();
        return homePage;
    }
}
