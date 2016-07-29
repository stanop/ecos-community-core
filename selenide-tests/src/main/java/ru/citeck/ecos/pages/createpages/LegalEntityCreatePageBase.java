package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class LegalEntityCreatePageBase {

    public void setFullOrganizationName(String value)
    {
        $("[id *= \"body-idocs_fullOrganizationName\"]").shouldBe(present).setValue(value);
    }
    public void setJuridicalAddress(String value)
    {
        $("[id *= \"idocs_juridicalAddress\"]").shouldBe(present).setValue(value);
    }
    public void setPostAddress(String value)
    {
        $("[id *= \"idocs_postAddress\"]").shouldBe(present).setValue(value);
    }
    public void clickOnButtonCreate()
    {
        $("[id *= \"body-form-submit\"]").shouldBe(enabled).click();
    }
}
