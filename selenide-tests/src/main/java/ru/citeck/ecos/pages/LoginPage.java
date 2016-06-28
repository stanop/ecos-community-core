package ru.citeck.ecos.pages;

import ru.citeck.ecos.Settings;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Condition.present;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;

public class LoginPage {

    public LoginPage()
    {
        Selenide.open("");

    }
    public void inLoginAndPassword(String username, String password) {
        $(By.name("username")).shouldBe(present).setValue(username);
        $(By.name("password")).shouldBe(present).setValue(password);
    }
    public HomePage pressEnter()
    {
        $(By.tagName("button")).click();
        $("#HEADER_TITLE").shouldBe(present);
        HomePage homePage = new HomePage();
        return homePage;
    }
}
