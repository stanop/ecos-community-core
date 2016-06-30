package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class ClosingDocumentCreatePage extends CreatePage{

    public void openCreatePage()
    {
        open("/node-create-page?type=contracts:closingDocument");
    }
    public void setNameClosingDocument()
    {
        $("[id *= \"default-cm_name\"]").setValue("Act"+Math.random()).shouldBe(present);
    }
    @Override
    public void setDocumentNumber()
    {
        $("[id *= \"contracts_closingDocumentNumber\"]").setValue("№"+Math.random()*100).shouldBe(present);
    }
    public void selectPayment()
    {
        $("[id *= \"contracts_closingDocumentPayment-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"closingDocumentPayment-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"closingDocumentPayment-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }
    public void selectOriginalLocation()
    {
        $("[id *= \"closingDocumentOriginalLocation-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"closingDocumentOriginalLocation-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"closingDocumentOriginalLocation-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }
    @Override
    public void setDocumentDate()
    {
        $("[id *= \"closingDocumentDate\"] input").setValue("2016-06-29").shouldBe(present);
    }
    public void selectContract()
    {
        $("[id *= \"closingDocumentAgreement-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"closingDocumentAgreement-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"closingDocumentAgreement-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }
}
