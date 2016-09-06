package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.JavascriptExecutor;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.title;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class DocumentDetailsPage extends PageBase{

    public String getTitle() {
        return title();
    }

    public StartWorkflowPage openStartWorkflowPage() {
        $("#onServerAction > a").shouldBe(present).click();
        StartWorkflowPage startWorkflowPage = new StartWorkflowPage();
        return startWorkflowPage;
    }

    public String getNumberAgreement() {
        return $$(".value-item-text").get(5).shouldBe(present).attr("title").toString();
    }

    public String getNumberSupplementaryAgreement() {
        return $("span.value-item-text").shouldBe(present).attr("title").toString();
    }

    public String getNumberPayment()
    {
        return $$("span.value-item-text").get(2).shouldBe(present).attr("title").toString();
    }

    public void performTaskConfirm(String message) {
        $(".viewmode-value").shouldHave(text(message));
        $("[id *= \"confirmOutcome-Confirmed-button\"]").shouldBe(enabled).click();
    }

    public void performTaskSign() {
        $("[id *= \"signOutcome-Signed-button\"]").shouldBe(enabled).click();
    }

    public void performTaskAffirm() {
        $("[id *= \"affirmOutcome-Affirmed-button\"]").shouldBe(enabled).click();
    }

    public SelenideElement getStatusDocument() {
        SelenideElement element = $("span.panel-body");
        String js = "arguments[0].scrollIntoView();";
        ((JavascriptExecutor)getWebDriver()).executeScript(js, element);
        return element.shouldBe(present);
    }

    public void clickOnActionMoveToArchive() {
        $("#onServerAction a").shouldHave(text("Move to archive")).click();
        $(".value-item-text").waitUntil(present,20000);
    }
}
