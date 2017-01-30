package ru.citeck.ecos.pages.createpages;

import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.homepagessites.HomePageSiteContracts;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PaymentCreatePage extends CreatePageBase {

    public void openCreatePagePayment() {
        HomePageSiteContracts homePageSiteContracts = new HomePageSiteContracts();
        homePageSiteContracts.openCreateFormPayment();
    }

    public void setPaymentFor(String paymentFor) {
        $("[id *= \"payments_paymentFor\"]").selectOptionByValue(paymentFor);
    }

    @Override
    public void setDocumentNumber(String number) {
        $("[id *= \"paymentNumber\"]").setValue(number).shouldBe(present);
    }

    public ContractCreatePage openContractCreatePage(String nameDocumentBase) {
        $("[id *= \"payments_basis-journalControl-button\"]").shouldBe(present).shouldBe(enabled).click();
        $(".create.yui-button.yui-menu-button").shouldBe(present).shouldBe(enabled).click();
        $(byText(nameDocumentBase)).shouldBe(present).shouldBe(enabled).click();
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        return contractCreatePage;
    }

    public  SupplementaryAgreementCreatePage openSupplementaryAgreementCreatePage(String nameDocumentBase) {
        $("[id *= \"payments_basis-journalControl-button\"]").shouldBe(present).shouldBe(enabled).click();
        $(".create.yui-button.yui-menu-button").shouldBe(present).shouldBe(enabled).click();
        $(byText(nameDocumentBase)).shouldBe(present).shouldBe(enabled).click();
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        return supplementaryAgreementCreatePage;
    }

    public SelenideElement selectDocumentBase() {
        $("[id *= \"payments_basis-journalControl-journalPanel-selectedElementsTable\"] table td").shouldBe(present);
        $("[id *= \"payments_basis-journalControl-journalPanel-submitInput\"]").shouldBe(enabled).click();
        return $$(".value-item").get(0);
    }

    public ProductsAndServicesCreatePage openProductOrServiceCreatePage() {
        $("[id *= \"containsOriginalProductsAndServices-journalControl-button\"]").shouldBe(present).shouldBe(enabled).click();
        $(".create-object-button").shouldBe(present).shouldBe(enabled).click();
        ProductsAndServicesCreatePage productsAndServicesCreatePage = new ProductsAndServicesCreatePage();
        return productsAndServicesCreatePage;
    }

}
