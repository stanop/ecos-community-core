package ru.citeck.ecos.pages.createpages;


import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.DocumentDetailsPage;
import ru.citeck.ecos.pages.HomePage;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ContractCreatePage extends CreatePageBase {

    public void openCreatePageContract()
    {
        HomePage homePage = new HomePage();
        HomePageSiteContracts homePageSiteContracts =  homePage.getMenu().goToContracts();
        homePageSiteContracts.openCreateFormContracts();
    }
    public void setContractWith(String value)
    {
        $("[id *= \"contracts_contractWith\"]").shouldBe(present).selectOptionByValue(value);
    }
    public void setAgreementAmount(String value)
    {
        $("[id *= \"contracts_agreementAmount\"]").shouldBe(present).setValue(value);
    }
    public void setVAT(String value)
    {
        $("[id *= \"contracts_VAT\"]").shouldBe(present).setValue(value);
    }
    public void setSummary(String value)
    {
        $("[id *= \"idocs_summary\"]").shouldBe(present).setValue(value);
    }
    public void setNode(String value)
    {
      $("[id *= \"idocs_note\"]").shouldBe(present).setValue(value);
    }
    public void setNumberOfAppendixPage(String value)
    {
        $("[id *= \"idocs_appendixPagesNumber\"]").shouldBe(present).setValue(value);
    }
    public void setNumberPage(String value)
    {
        $("[id *= \"idocs_pagesNumber\"]").shouldBe(present).setValue(value);
    }
    public void createPaymentSchedule(String value)
    {
        $("[id *= \"payments_payments-createObjectControl\"] button").shouldBe(present).click();
        $("[id *= \"payments_paymentAmount\"]").shouldBe(present).setValue(value);
        $("[id *= \"form-submit\"]").shouldBe(present).click();
    }
    public DocumentDetailsPage clickOnCreateContractButton()
    {
        $("[id *= \"form-submit\"]").shouldBe(present).click();
        DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
        return documentDetailsPage;
    }
    public SelenideElement selectSignatory(String userName)
    {
        $("[id *= \"signatory-orgstructControl-showVariantsButton\"]").shouldBe(enabled).click();
        $("[id *= \"signatory-orgstructControl-orgstructPanel-searchInput\"]").shouldBe(present).setValue(userName).pressEnter();
        $("[id *= \"signatory-orgstructControl-orgstructPanel\"] table tr td[id *= \"contente\"]").shouldHave(text(userName)).click();
        $(".selected-object.authorityType-USER").shouldBe(present);
        $("[id *= \"signatory-orgstructControl-orgstructPanel-submitInput\"]").shouldBe(present).click();
        return $$(".value-item").get(1);
    }
    public SelenideElement selectPerformer(String userName)
    {
        $("[id *= \"performer-orgstructControl-showVariantsButton\"]").shouldBe(enabled).click();
        $("[id *= \"idocs_performer-orgstructControl-orgstructPanel-searchInput\"]").shouldBe(present).setValue(userName).pressEnter();
        $("[id *= \"performer-orgstructControl-orgstructPanel\"] table tr td[id *= \"contente\"]").shouldHave(text(userName)).click();
        $(".selected-object.authorityType-USER").shouldBe(present);
        $("[id *= \"performer-orgstructControl-orgstructPanel-submitInput\"]").shouldBe(present).click();
        return $$(".value-item").get(2);
    }
    public AgreementSubjectCreatePage openAgreementSubjectCreatePage()
    {
        $("[id *= \"agreementSubject-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"agreementSubject-journalControl-journalPanel-journal-picker-header\"] .create-object-button").shouldBe(enabled).click();
        AgreementSubjectCreatePage agreementSubjectCreatePage = new AgreementSubjectCreatePage();
        return agreementSubjectCreatePage;
    }
    public void selectAgreementSubject()
    {
        $("[id *= \"agreementSubject-journalControl-journalPanel-selectedElementsTable\"] table td").shouldBe(present);
        $("[id *= \"contracts_agreementSubject-journalControl-journalPanel-submitInput\"]").shouldBe(enabled).click();
        //return $$(".value-item").get(1);
    }
    public PaymentScheduleCreatePage openPaymentScheduleCreatePage()
    {
        $("[id *= \"payments-createObjectControl\"] button").shouldBe(enabled).click();
        PaymentScheduleCreatePage paymentSchedule = new PaymentScheduleCreatePage();
        return paymentSchedule;
    }
    public void createPaymentSchedule()
    {
        $("[id *= \"body-form-submit\"]").shouldBe(enabled).click();
    }

}
