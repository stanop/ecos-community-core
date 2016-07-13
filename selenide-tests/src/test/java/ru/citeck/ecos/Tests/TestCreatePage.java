package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.createpages.*;
import ru.citeck.ecos.pages.DocumentDetailsPage;
import org.junit.Assert;
import org.junit.Test;

import static com.codeborne.selenide.Condition.visible;

public class TestCreatePage extends SelenideTests{
    @Test
    public void createContract()
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        contractCreatePage.openNewCreatePage();
        contractCreatePage.clickOnBCancelButton();

        contractCreatePage.openNewCreatePage();
        contractCreatePage.setLegalEntity();
        contractCreatePage.setContractor();
        contractCreatePage.setContractWith();
        contractCreatePage.setDocumentNumber();
        contractCreatePage.setDocumentDate();
        contractCreatePage.selectCurrency();
        contractCreatePage.setSummary();
//        contractCreatePage.setContractValue();//bug ECOSCOM-453
//        contractCreatePage.setVAT();
//        contractCreatePage.setNumberOfAppendixPage();
//        contractCreatePage.setNumberPage();
//        contractCreatePage.createPaymentSchedule();
//        SelenideElement resetButton = contractCreatePage.clickOnResetButton();
//        resetButton.shouldBe(visible);
//
//        contractCreatePage.setLegalEntityFormCreateContract();
        contractCreatePage.setContractor();
//        contractCreatePage.setContractWith();
        contractCreatePage.setDocumentNumber();
        contractCreatePage.setDocumentDate();
//        contractCreatePage.selectCurrency();
        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createSupplementaryAgreement()
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePage();
        supplementaryAgreementCreatePage.clickOnBCancelButton();

        supplementaryAgreementCreatePage.openCreatePage();
        supplementaryAgreementCreatePage.setLegalEntity();
        supplementaryAgreementCreatePage.setDocumentNumber();
        SelenideElement resetButton = supplementaryAgreementCreatePage.clickOnResetButton();
        resetButton.shouldBe(visible);

        supplementaryAgreementCreatePage.setLegalEntity();
        supplementaryAgreementCreatePage.setContractor();
        supplementaryAgreementCreatePage.setDocumentNumber();
        supplementaryAgreementCreatePage.setDocumentDate();
        supplementaryAgreementCreatePage.selectMainAgreement();
        DocumentDetailsPage documentDetailsPage = supplementaryAgreementCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createClosingDocumentWithContact()
    {
        ClosingDocumentCreatePage closingDocumentCreatePage = new ClosingDocumentCreatePage();
        closingDocumentCreatePage.openCreatePage();
        closingDocumentCreatePage.clickOnBCancelButton();

        closingDocumentCreatePage.openCreatePage();
        closingDocumentCreatePage.setNameClosingDocument();
        closingDocumentCreatePage.selectContract();
        closingDocumentCreatePage.clickOnResetButton();

        closingDocumentCreatePage.setNameClosingDocument();
        closingDocumentCreatePage.selectContract();
        closingDocumentCreatePage.selectOriginalLocation();
        closingDocumentCreatePage.setDocumentNumber();
        closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.setDocumentDate();
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createClosingDocumentWithoutContract()
    {
        ClosingDocumentCreatePage closingDocumentCreatePage = new ClosingDocumentCreatePage();
        closingDocumentCreatePage.openCreatePage();
        closingDocumentCreatePage.setNameClosingDocument();
        closingDocumentCreatePage.selectOriginalLocation();
        closingDocumentCreatePage.setLegalEntity();
        closingDocumentCreatePage.setContractor();
        closingDocumentCreatePage.setDocumentNumber();
        closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.setDocumentDate();
        closingDocumentCreatePage.selectCurrency();
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createPayment()
    {
        PaymentCreatePage paymentCreatePage = new PaymentCreatePage();
        paymentCreatePage.openCreatePage();
        paymentCreatePage.clickOnBCancelButton();

        paymentCreatePage.openCreatePage();
        paymentCreatePage.setLegalEntity();
        paymentCreatePage.setContractor();
        paymentCreatePage.setDocumentNumber();
        paymentCreatePage.setPaymentFor();
        paymentCreatePage.selectProductOrServise();
        DocumentDetailsPage documentDetailsPage = paymentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }

}
