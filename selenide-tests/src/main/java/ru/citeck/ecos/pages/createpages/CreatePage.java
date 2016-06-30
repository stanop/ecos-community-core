package  ru.citeck.ecos.pages.createpages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import  ru.citeck.ecos.pages.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;

public class CreatePage {

    public String getTitle() {
        return title();
    }


    public void setLegalEntityFormCreateContract()
    {
        $("[id *= \"egalEntity-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"egalEntity-journalControl-journalPanel-selectedElementsTable\"]").shouldBe(present);
        $("[id *= \"egalEntity-journalControl-journalPanel-submitInput\"]").click();
    }
    public void setContractorFormCreateContract()
    {
        $(By.cssSelector("[id *= \"contractor-journalControl-button\"]")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"workspace\"]")).shouldBe(present).click();
        $("[id *= \"contractor-journalControl-journalPanel-selectedElementsTable\"]").shouldBe(present);
        $(By.cssSelector("[id *= \"contractor-journalControl-journalPanel-submitInput\"]")).click();
    }
    public void setDocumentNumber()
    {
        $(By.cssSelector("[id *= \"agreementNumber\"]")).setValue("№"+Math.random()*100).shouldBe(present);
    }
    public void selectCurrency()
    {
        $("[data-bind *= \"clear \"]").click();
        $(".autocomplete-twister").click();
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
        $(By.cssSelector("[id *= \"form-cancel\"]")).shouldBe(present).click();
    }
    public SelenideElement clickOnResetButton()
    {
        $("[id *= 'form-reset']").shouldBe(present).click();
        return $("span.validation-message");
    }
    public void setDocumentDate()
    {
        $(By.cssSelector("[id *= \"contracts_agreementDate\"] input")).setValue("2016-06-29").shouldBe(present);
    }
}
