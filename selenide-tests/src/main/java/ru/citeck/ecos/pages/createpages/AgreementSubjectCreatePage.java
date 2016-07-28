package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class AgreementSubjectCreatePage {
    public void setSubjectCode(String value)
    {
        $("[id *= \"body-contracts_subjectCode\"]").shouldBe(present).setValue(value);
    }
    public void setSubjectName(String value)
    {
        $("[id *= \"body-contracts_subjectName\"]").shouldBe(present).setValue(value);
    }
    public void clickOnButtonCreate()
    {
        $("[id *= \"body-form-submit\"]").shouldBe(enabled).click();
    }
}
