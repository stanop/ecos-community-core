package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
import ru.citeck.ecos.pages.createpages.*;
import ru.citeck.ecos.pages.DocumentDetailsPage;
import org.junit.Assert;
import org.junit.Test;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.sleep;

public class TestCreatePage extends SelenideTests{
    @Test
    public void createContract()
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        contractCreatePage.openCreatePageContract();

        contractCreatePage.clickOnButtonGenerate();

        LegalEntityCreatePage legalEntityCreatePage =  contractCreatePage.openCreatePageLegalEntity();
        legalEntityCreatePage.setFullOrganizationName("testLegalEntity");
        legalEntityCreatePage.setJuridicalAddress("address1");
        legalEntityCreatePage.setPostAddress("address2");
        legalEntityCreatePage.setINN("18746321");
        legalEntityCreatePage.setKPP("1274639983");
        legalEntityCreatePage.clickOnButtonCreate();
        SelenideElement legalEntityValue = contractCreatePage.setLegalEntity("testLegalEntity");
        legalEntityValue.shouldHave(text("testLegalEntity"));

        ContractorCreatePage contractorCreatePage = contractCreatePage.openContractorCreatePage();
        contractorCreatePage.setFullOrganizationName("testContractor");
        contractorCreatePage.setJuridicalAddress("address3");
        contractorCreatePage.setPostAddress("address3");
        contractorCreatePage.setCeoName("Ivanov I.I.");
        contractorCreatePage.clickOnButtonCreate();
        SelenideElement contractor = contractCreatePage.setContractor("testContractor");
        contractor.shouldHave(text("testContractor"));

        contractCreatePage.setContractWith("client");
        contractCreatePage.selectKindDocument("Услуги");
       // contractCreatePage.setDocumentNumber(""+Math.random());
        contractCreatePage.setDocumentDate("2016-07-14");
        contractCreatePage.setSummary("Summary");

        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||  "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createSupplementaryAgreement()
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePageSupplementaryAgreement();

        supplementaryAgreementCreatePage.clickOnButtonMainAgreement();
        ContractCreatePage mainAgreement = supplementaryAgreementCreatePage.openMainContractCreatePage();
        mainAgreement.clickOnButtonGenerate();

        LegalEntityCreatePage legalEntityCreatePage =  mainAgreement.openCreatePageLegalEntity();
        legalEntityCreatePage.setFullOrganizationName("testLegalEntity");
        legalEntityCreatePage.setJuridicalAddress("address1");
        legalEntityCreatePage.setPostAddress("address2");
        legalEntityCreatePage.setINN("18746321");
        legalEntityCreatePage.setKPP("1274639983");
        legalEntityCreatePage.clickOnButtonCreate();
        SelenideElement legalEntityValue = mainAgreement.setLegalEntity("testLegalEntity");
        legalEntityValue.shouldHave(text("testLegalEntity"));

        ContractorCreatePage contractorCreatePage = mainAgreement.openContractorCreatePage();
        contractorCreatePage.setFullOrganizationName("testContractor");
        contractorCreatePage.setJuridicalAddress("address3");
        contractorCreatePage.setPostAddress("address3");
        contractorCreatePage.setCeoName("Ivanov I.I.");
        contractorCreatePage.clickOnButtonCreate();
        SelenideElement contractor = mainAgreement.setContractor("testContractor");
        contractor.shouldHave(text("testContractor"));

        mainAgreement.setContractWith("client");
        mainAgreement.selectKindDocument("Услуги");
        mainAgreement.setDocumentDate("2016-07-14");
        mainAgreement.setSummary("Summary");
        mainAgreement.clickOnCreateContractButton();
        SelenideElement mainAgreementValue = supplementaryAgreementCreatePage.selectMainAgreement();
        mainAgreementValue.shouldBe(present);

        supplementaryAgreementCreatePage.setDocumentNumber(""+Math.random());
        supplementaryAgreementCreatePage.setDocumentDate("2016-07-14");

        DocumentDetailsPage documentDetailsPage = supplementaryAgreementCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||  "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createClosingDocumentWithContact()
    {
        ClosingDocumentCreatePage closingDocumentCreatePage = new ClosingDocumentCreatePage();
        closingDocumentCreatePage.openCreatePageClosingDocument();

        ContractCreatePage contractCreatePage = closingDocumentCreatePage.openContractCreatePage();
        contractCreatePage.clickOnButtonGenerate();

        LegalEntityCreatePage legalEntityCreatePage =  contractCreatePage.openCreatePageLegalEntity();
        legalEntityCreatePage.setFullOrganizationName("testLegalEntity");
        legalEntityCreatePage.setJuridicalAddress("address1");
        legalEntityCreatePage.setPostAddress("address2");
        legalEntityCreatePage.setINN("18746321");
        legalEntityCreatePage.setKPP("1274639983");
        legalEntityCreatePage.clickOnButtonCreate();
        SelenideElement legalEntityValue = contractCreatePage.setLegalEntity("testLegalEntity");
        legalEntityValue.shouldHave(text("testLegalEntity"));

        ContractorCreatePage contractorCreatePage = contractCreatePage.openContractorCreatePage();
        contractorCreatePage.setFullOrganizationName("testContractor");
        contractorCreatePage.setJuridicalAddress("address3");
        contractorCreatePage.setPostAddress("address3");
        contractorCreatePage.setCeoName("Ivanov I.I.");
        contractorCreatePage.clickOnButtonCreate();
        contractCreatePage.setContractor("testContractor");

        contractCreatePage.setContractWith("client");
        contractCreatePage.selectKindDocument("Услуги");
        // contractCreatePage.setDocumentNumber(""+Math.random());
        contractCreatePage.setDocumentDate("2016-07-14");
        contractCreatePage.setSummary("Summary");
        contractCreatePage.clickOnCreateContractButton();
        closingDocumentCreatePage.selectContract();

        closingDocumentCreatePage.setDocumentNumber(""+Math.random());
        //closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.setDocumentDate("2016-07-14");
        closingDocumentCreatePage.selectKindDocument("Акт");
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||  "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createClosingDocumentWithoutContract()
    {
        ClosingDocumentCreatePage closingDocumentCreatePage = new ClosingDocumentCreatePage();
        closingDocumentCreatePage.openCreatePageClosingDocument();

        OriginalLocationCreatePage originalLocation = closingDocumentCreatePage.openCreatePageOriginalLocation();
        originalLocation.senName("testOriginalLocation");
        originalLocation.clickOnButtonCreate();
        closingDocumentCreatePage.selectOriginalLocation();

        LegalEntityCreatePage legalEntity = closingDocumentCreatePage.openCreatePageLegalEntity();
        legalEntity.setFullOrganizationName("testLegalEntity");
        legalEntity.setJuridicalAddress("Address 1");
        legalEntity.setPostAddress("Address 33");
        legalEntity.setINN("123455531");
        legalEntity.setKPP("684768322");
        legalEntity.clickOnButtonCreate();
        closingDocumentCreatePage.setLegalEntity("testLegalEntity");

        ContractorCreatePage contractor = closingDocumentCreatePage.openContractorCreatePage();
        contractor.setJuridicalAddress("Address, 111");
        contractor.setFullOrganizationName("testContractor");
        contractor.setPostAddress("Address, addres 2");
        contractor.setCeoName("Ivanov TC");
        contractor.clickOnButtonCreate();
        closingDocumentCreatePage.setContractor("testContractor");

        closingDocumentCreatePage.clickOnButtonGenerate();

//        PaymentCreatePage payment = closingDocumentCreatePage.openPaymentCreatePage();
//        closingDocumentCreatePage.selectPayment();
        closingDocumentCreatePage.setDocumentDate("2016-07-14");
        closingDocumentCreatePage.selectKindDocument("Накладная");
        //closingDocumentCreatePage.selectCurrency("Доллар");
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||  "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
    }
//    @Test
//    public void createPayment()
//    {
//        PaymentCreatePage paymentCreatePage = new PaymentCreatePage();
//        paymentCreatePage.openCreatePagePayment();
//        paymentCreatePage.clickOnBCancelButton();
//
//        paymentCreatePage.openCreatePagePayment();
//        paymentCreatePage.setLegalEntity("testLegalEntity");
//        paymentCreatePage.setContractor("testContractor");
//        paymentCreatePage.setDocumentNumber("№"+Math.random());
//        paymentCreatePage.setPaymentFor();
//        paymentCreatePage.selectProductOrService();
//        DocumentDetailsPage documentDetailsPage = paymentCreatePage.clickOnCreateContentButton();
//        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||  "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
//    }

}
