package ru.citeck.ecos.main;

import com.codeborne.selenide.Condition;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.*;

public class MainTest {

    @Test
    public void openURL() {
//        System.setProperty("selenide.timeout", "7500"); //Don't use
//        System.setProperty("selenide.baseUrl", "http://37.230.155.222:4580");
        open("/share/page/");
        $(By.name("username")).val("ivan");
        $(By.name("password")).val("ivan").pressEnter();
        $(By.className("error")).shouldNotBe(Condition.visible);
        $(("#HEADER_USER_MENU_POPUP_text")).shouldHave(Condition.hasText("ivan"));
    }

}
