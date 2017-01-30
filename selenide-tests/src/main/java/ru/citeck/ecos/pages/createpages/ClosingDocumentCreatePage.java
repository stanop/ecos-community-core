package ru.citeck.ecos.pages.createpages;


import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class ClosingDocumentCreatePage extends CreatePageBase {

    public void openCreatePageClosingDocument() {
        HomePageSiteContracts homePageSiteContracts = new HomePageSiteContracts();
        homePageSiteContracts.openCreateFormClosingDocument();
    }

    public ContractCreatePage openContractCreatePage(String nameDocumentBase) {
        $("[id *= \"closingDocumentAgreement-journalControl-button\"]").shouldBe(present).shouldBe(enabled).click();
        $(".create.yui-button.yui-menu-button").shouldBe(present).shouldBe(enabled).click();
        $(byText(nameDocumentBase)).shouldBe(present).shouldBe(enabled).click();
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        return contractCreatePage;
    }

    public SupplementaryAgreementCreatePage openSupplementaryAgreementCreatePage(String nameDocumentBase) {
        $("[id *= \"closingDocumentAgreement-journalControl-button\"]").shouldBe(present).shouldBe(enabled).click();
        $(".create.yui-button.yui-menu-button").shouldBe(present).shouldBe(enabled).click();
        $(byText(nameDocumentBase)).shouldBe(present).shouldBe(enabled).click();
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        return supplementaryAgreementCreatePage;
    }

    public void selectDocumentBase() {
        SelenideElement selectedDocumentBase
                = $("[id *= \"closingDocumentAgreement-journalControl-journalPanel-selectedElementsTable\"] table td");
        selectedDocumentBase.shouldBe(present);
        $("[id *= \"closingDocumentAgreement-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }

    public PaymentCreatePage openPaymentCreatePage() {
        $("[id *= \"contracts_closingDocumentPayment-journalControl-button\"]").shouldBe(enabled).click();
        SelenideElement buttonCreate
                = $("[id *= \"contracts_closingDocumentPayment-journalControl-journalPanel-journal-picker-header\"] " +
                ".create-object-button");
        buttonCreate.shouldBe(present).shouldBe(enabled).click();
        PaymentCreatePage payment = new PaymentCreatePage();
        return payment;
    }

    public void selectPayment() {
        SelenideElement selectedPayment
                = $("[id *= \"closingDocumentPayment-journalControl-journalPanel-selectedElementsTable\"] table td");
        selectedPayment.shouldBe(present);
        $("[id *= \"closingDocumentPayment-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }

    public OriginalLocationCreatePage openCreatePageOriginalLocation() {
        $("[id *= \"closingDocumentOriginalLocation-journalControl-button\"]").shouldBe(present).click();
        SelenideElement buttonCreate
                = $("[id *= \"contracts_closingDocumentOriginalLocation-journalControl\"] .create-object-button");
        buttonCreate.should(enabled).click();
        OriginalLocationCreatePage originalLocation = new OriginalLocationCreatePage();
        return originalLocation;
    }

    public void selectOriginalLocation() {
        SelenideElement selectedOriginalLocation
                = $("[id *= \"closingDocumentOriginalLocation-journalControl-journalPanel-selectedElementsTable\"] " +
                "table tbody");
        selectedOriginalLocation.shouldBe(present);
        SelenideElement buttonOK
                = $("[id *= \"closingDocumentOriginalLocation-journalControl-journalPanel-submitInput\"]");
        buttonOK.shouldBe(present).click();
    }

    @Override
    public void setDocumentDate(String date) {
        $("[id *= \"closingDocumentDate\"] input").setValue(date).shouldBe(present);
    }

    public void setNameClosingDocument(String name)
    {
        $("[id *= \"cm_name\"]").shouldBe(present).setValue(name);
    }
}
