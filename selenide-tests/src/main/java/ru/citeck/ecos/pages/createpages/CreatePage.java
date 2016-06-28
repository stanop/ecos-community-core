package  ru.citeck.ecos.pages.createpages;


import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import  ru.citeck.ecos.pages.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;

public class CreatePage {

    public SelenideElement getTitle() {
        return $("#HEADER_TITLE");
    }


    public void setLegalEntityFormCreateContract()
    {
        $(By.cssSelector("[id *= \"egalEntity-journalControl-button\"]")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"workspace\"]")).click();
        $(By.cssSelector("[id *= \"contracts_agreementLegalEntity-journalControl-journalPanel-submitInput\"]")).click();
    }
    public void setContractorFormCreateContract()
    {
        $(By.cssSelector("[id *= \"contractor-journalControl-button\"]")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"workspace\"]")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"contractor-journalControl-journalPanel-submitInput\"]")).click();
    }
    public void selectCurrency()
    {
        $("[data-bind *= \"clear \"]").click();
        $(".autocomplete-twister").click();
       // $("[data-bind *= 'searching']").setValue("руб").shouldBe(present).click();
        $("input.autocomplete-search").setValue("руб").shouldBe(present);
        $("ul.autocomplete-list > li.selected > a").click();
    }


    public DocumentDetailsPage clickOnCreateContentButton()
    {
        DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
        $(By.cssSelector("[id *= \"form-submit\"]")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"document-actions\"]")).shouldBe(present);
        return documentDetailsPage;
    }
    public void clickOnBCancelButton()
    {
        $(By.cssSelector("[id *= \"form-cancel\"]")).click();
    }
    public SelenideElement clickOnResetButton()
    {
        $("[id *= 'form-reset']").click();
        return $("span.validation-message");
    }
}
