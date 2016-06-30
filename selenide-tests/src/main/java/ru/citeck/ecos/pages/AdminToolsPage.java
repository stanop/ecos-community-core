package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.*;

public class AdminToolsPage {


    public String getTitle()
    {
       return title();
    }

    public SelenideElement openUserContent()
    {
        $("#alf-filters [href = \"users\"]").shouldBe(present).click();
        return $(By.cssSelector("#alf-content > div > div > div > div")).shouldBe(present);
    }
    public SelenideElement clickOnButtonNewUser()
    {
        $(By.cssSelector(".newuser-button button")).shouldBe(present).click();
        return $(By.cssSelector(".create-main")).shouldBe(present);
    }
    public void setValueOnFromCreateNewUser(String username, String login, String password)
    {
        $(By.cssSelector("[id*=\"create-firstname\"]")).shouldBe(present).setValue(username);
        $(By.cssSelector("[id*=\"create-email\"]")).shouldBe(present).setValue("test1@test.ru");
        $(By.cssSelector("[id*=\"create-username\"]")).shouldBe(present).setValue(login);
        $(By.cssSelector("[id*=\"create-password\"]")).shouldBe(present).setValue(password);
        $(By.cssSelector("[id*=\"create-verifypassword\"]")).shouldBe(present).setValue(password);

    }
    public void clickOnButtonCreate()
    {
        $(By.cssSelector("[id*=\"createuser-ok-button-button\"]")).shouldBe(present).click();
    }
    public SelenideElement searchUser(String login)
    {
        $(By.cssSelector("[id*=\"default-search-text\"]")).shouldBe(present).setValue(login).pressEnter();
        return $(By.cssSelector(".yui-dt-data > tr")).shouldBe(present);
    }

    public LoginPage logOut()
    {
        LoginPage loginPage = new LoginPage();
        $(By.cssSelector("#HEADER_USER_MENU_POPUP_text")).shouldBe(present).click();
        $(By.cssSelector("#HEADER_USER_MENU_LOGOUT_text")).shouldBe(present).click();
        return loginPage;
    }
}
