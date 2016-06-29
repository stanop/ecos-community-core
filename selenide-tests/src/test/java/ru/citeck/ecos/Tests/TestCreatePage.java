package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.createpages.ClosingDocumentCreatePage;
import ru.citeck.ecos.pages.createpages.ContractCreatePage;
import ru.citeck.ecos.pages.createpages.CreatePage;
import ru.citeck.ecos.pages.createpages.SupplementaryAgreementCreatePage;
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
        contractCreatePage.setLegalEntityFormCreateContract();
        contractCreatePage.setContractorFormCreateContract();
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
        contractCreatePage.setContractorFormCreateContract();
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
        supplementaryAgreementCreatePage.setLegalEntityFormCreateContract();
//        supplementaryAgreementCreatePage.setContractorFormCreateContract();
        supplementaryAgreementCreatePage.setDocumentNumber();
//        supplementaryAgreementCreatePage.setDocumentDate();
//        supplementaryAgreementCreatePage.selectMainAgreement();
        SelenideElement resetButton = supplementaryAgreementCreatePage.clickOnResetButton();
        resetButton.shouldBe(visible);

        supplementaryAgreementCreatePage.setLegalEntityFormCreateContract();
        supplementaryAgreementCreatePage.setContractorFormCreateContract();
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
        closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.selectOriginalLocation();
        closingDocumentCreatePage.setDocumentNumber();
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
        closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.selectOriginalLocation();
        closingDocumentCreatePage.setLegalEntityFormCreateContract();
        closingDocumentCreatePage.setContractorFormCreateContract();
        closingDocumentCreatePage.setDocumentNumber();
        closingDocumentCreatePage.setDocumentDate();
        closingDocumentCreatePage.selectCurrency();
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Document Details".equals(documentDetailsPage.getTitle()));
    }

}
