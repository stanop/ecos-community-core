package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;

public class AdminToolsPage extends PageBase{
    public String getTitle()
    {
        return title();
    }

    public SelenideElement openUserContent()
    {
        $("#alf-filters [href = \"users\"]").click();
        return $("#alf-content > div > div > div > div");
    }
    public SelenideElement clickOnButtonNewUser()
    {
        $(".newuser-button button").click();
        return $(".create-main");
    }
    public void setValueOnFromCreateNewUser(String username, String login, String password)
    {
        $("[id*=\"create-firstname\"]").setValue(username);
        $("[id*=\"create-email\"]").setValue("test1@citeck.ru");
        $("[id*=\"create-username\"]").setValue(login);
        $("[id*=\"create-password\"]").setValue(password);
        $("[id*=\"create-verifypassword\"]").setValue(password);

    }
    public void selectGroup(String idGroup)
    {
        $("[id *= \"create-groupfinder-search-text\"]").shouldBe(present).setValue(idGroup).pressEnter();
        $("[headers *= \"th-action\"]").shouldBe(enabled).click();
        //$("[id *= \"group1\"]").shouldBe(present);
        //return $("[id *= \"group1\"]");
    }
    public void clickOnButtonCreate()
    {
        $("[id*=\"createuser-ok-button-button\"]").click();
    }
    public SelenideElement searchUser(String login)
    {
        $("[id*=\"default-search-text\"]").setValue(login).pressEnter();
        return $(".yui-dt-data > tr");
    }
    public void clickOnUserName(String userName)
    {
        $("[headers *= \"fullName\"] a").shouldHave(text(userName)).click();
    }
    public void clickOnButtonDeleteUser()
    {
        $("button[id *= \"deleteuser-button\"]").shouldBe(enabled).click();
        $("#deleteDialog button").shouldBe(enabled).click();
    }
}
