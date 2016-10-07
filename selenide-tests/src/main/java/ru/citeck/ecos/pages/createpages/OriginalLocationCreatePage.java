package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class OriginalLocationCreatePage {

    public void setName(String value) {
        $("[id *= \"body-cm_name\"]").shouldBe(present).setValue(value);
    }

    public void clickOnButtonCreate() {
        $("[id *= \"body-form-submit\"]").shouldBe(enabled).click();
    }
}
