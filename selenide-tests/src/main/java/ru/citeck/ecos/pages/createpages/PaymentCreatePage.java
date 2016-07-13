package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class PaymentCreatePage extends CreatePage{
    public void openCreatePage()
    {
        open("/node-create-page?type=payments:payment");
    }

    public void setPaymentFor()
    {
        $("[id *= \"payments_paymentFor\"]").selectOptionByValue("client");
    }
    @Override
    public void setLegalEntity()
    {
        $("[id *= \"payments_payer-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"payments_payer-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"payments_payer-journalControl-journalPanel-submitInput\"]").click();
    }
    @Override
    public void setContractor()
    {
        $("[id *= \"payments_beneficiary-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"payments_beneficiary-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"payments_beneficiary-journalControl-journalPanel-submitInput\"]").click();
    }
    @Override
    public void setDocumentNumber()
    {
        $("[id *= \"paymentNumber\"]").setValue("№"+Math.random()*100).shouldBe(present);
    }
    public void selectProductOrServise()
    {
        $("[id *= \"containsOriginalProductsAndServices-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"containsOriginalProductsAndServices-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"containsOriginalProductsAndServices-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }

}
