package ru.citeck.ecos.pages;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class StartWorkflowPage {
    public void selectParticipant(String participant)
    {
        $("[id *= \"participant\"] > a").shouldBe(present).click();
        $("[id *= \"participant\"] input").shouldBe(present).setValue(participant).pressEnter();
        $("[id *= \"ygtvlabel\"]").shouldHave(text(participant)).click();
    }
    public void setWorkflowDescription(String description)
    {
        $("textarea[id *= \"prop_bpm_workflowDescription\"]").shouldBe(present).setValue(description);
    }
    public DocumentDetailsPage clickOnButtonStartApproval()
    {
        $("[id *= \"form-submit-button\"]").shouldBe(enabled).click();
        $(".value-item-text").shouldBe(present);
        DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
        return documentDetailsPage;
    }
}
