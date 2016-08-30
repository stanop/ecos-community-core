package  ru.citeck.ecos.pages.createpages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import  ru.citeck.ecos.pages.*;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class CreatePageBase {

    public String getTitle() {
        return title();
    }

    public LegalEntityCreatePage openCreatePageLegalEntity()
    {
        $("[id *= \"egalEntity-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"egalEntity-journalControl-journalPanel-journal-picker-header\"] .create-object-button").shouldBe(enabled).click();
        LegalEntityCreatePage legalEntityCreatePage = new LegalEntityCreatePage();
        return legalEntityCreatePage;
    }
    public SelenideElement setLegalEntity(String value)
    {
        $("[id *= \"egalEntity-journalControl-journalPanel-selectedElementsTable\"] table td").shouldHave(text(value));
        $("[id *= \"egalEntity-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
        return $$(".value-item").get(0);
    }

    public ContractorCreatePage openContractorCreatePage()
    {
        $("[id *= \"contractor-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"contractor-journalControl-journalPanel-journal-picker-header\"] .create-object-button").shouldBe(enabled).click();
        ContractorCreatePage contractorCreatePage = new ContractorCreatePage();
        return contractorCreatePage;
    }
    public SelenideElement setContractor(String value)
    {
        $("[id *= \"contractor-journalControl-journalPanel-selectedElementsTable\"] table td").shouldHave(text(value));
        $("[id *= \"contractor-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
        return $$(".value-item").get(1);
    }

    public void selectKindDocument(String value)
    {
        $("[id *= \"tk_kind-journalControl-button\"]").shouldBe(present).click();
        $(byText(value)).shouldBe(exist).click();
        $("[id *= \"tk_kind-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"tk_kind-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }
    public void setDocumentNumber(String number)
    {
        $("[id *= \"agreementNumber\"]").shouldBe(present).setValue(number);
    }
    public void clickOnButtonGenerate()
    {
        $("[name = \"number-generate\"]").shouldBe(present).click();

    }
    public void selectCurrency(String value)
    {
        $("[id *= \"Currency\"]").shouldBe(present).selectOption(value);
    }
    public DocumentDetailsPage clickOnCreateContentButton()
        {
            SelenideElement element = $("[id *= \"form-submit\"]");
            element.shouldBe(enabled).shouldBe(present).click();
            $(".value-item-text").shouldBe(present);
            DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
            return documentDetailsPage;
        }
    public void clickOnButtonCreate()
    {
        SelenideElement element = $("[id *= \"body-form-submit\"]");
        String js = "arguments[0].scrollIntoView();";
        ((JavascriptExecutor)getWebDriver()).executeScript(js, element);
        element.shouldBe(present).shouldBe(enabled).click();
    }
    public void setDocumentDate(String date)
    {
        $("[id *= \"agreementDate\"] input").shouldBe(present).setValue(date);
    }
    public void setDuration(String date)
    {
        $("[id *= \"duration\"]").shouldBe(present).setValue(date);
    }
}
