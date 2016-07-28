package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.title;

public class DocumentDetailsPage extends PageBase{

    public String getTitle()
    {
        return title();
    }
    public StartWorkflowPage openStartWorkflowPage()
    {
        $("#onServerAction > a").shouldBe(present).click();
        StartWorkflowPage startWorkflowPage = new StartWorkflowPage();
        return startWorkflowPage;
    }
    public String getNumberAgreement()
    {
        return $$(".value-item-text").get(5).shouldBe(present).attr("title").toString();
    }

    public void performTaskConfirm(String message)
    {
        $(".viewmode-value").shouldHave(text(message));
        $("[id *= \"confirmOutcome-Confirmed-button\"]").shouldBe(enabled).click();
    }
    public void performTaskSign(String message)
    {
        $("[id *= \"signOutcome-Signed-button\"]").shouldBe(enabled).click();
    }
    public SelenideElement getStatusDocument()
    {
        return $("span.panel-body").shouldBe(present);
    }
    public void clickOnActionMoveToArchive()
    {
        $("#onServerAction a").shouldHave(text("Move to archive")).click();
        $(".value-item-text").waitUntil(present,20000);
    }
}
