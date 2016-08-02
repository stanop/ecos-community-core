package ru.citeck.ecos.pages.createpages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.JavascriptExecutor;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.selected;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.getElement;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

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
        SelenideElement element = $("[id *= \"body-form-submit\"]");
        String js = "arguments[0].scrollIntoView();";
        ((JavascriptExecutor)getWebDriver()).executeScript(js, element);
        element.shouldBe(present).shouldBe(enabled).click();
    }
}
