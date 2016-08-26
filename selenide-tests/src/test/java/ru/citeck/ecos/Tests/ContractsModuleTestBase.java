package ru.citeck.ecos.Tests;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.*;
import ru.citeck.ecos.pages.createpages.*;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.sleep;

public class ContractsModuleTestBase extends SelenideTests{
    static protected String titleLoginPageRUS = "Citeck EcoS » Войти";
    static protected String titleLoginPageEN = "Citeck EcoS » Login";
    protected  String titleCardDetailsRUS = "Citeck EcoS » Карточка";
    protected String titleCardDetailsEN = "Citeck EcoS » Card details";
    protected String statusNew = "New";
    protected String statusOnApproval = "On approval";
    protected String statusOnSign = "On sign";
    protected String statusActive = "Active";
    protected String statusArchive = "Archive";

    static protected String userName = "Остап";
    static protected String login = "ostap";
    static protected String pass = "ostap";
    static protected String group = "company_director";
    static protected String UserNameAdmin = "Administrator";
    protected String loginAdmin = "Administrator";
    @BeforeClass
    static public void createUser()
    {
        createUser(userName, login, pass, group);
    }
    static protected void createUser(String username, String login, String password, String group)
    {
        HomePage homePage = new HomePage();
        AdminToolsPage adminToolsPage =  homePage.getMenu().openAdminTools();
        adminToolsPage.openUserContent().shouldBe(present);

        adminToolsPage.clickOnButtonNewUser().shouldBe(present);

        adminToolsPage.selectGroup(group);
        adminToolsPage.setValueOnFromCreateNewUser(username,login,password);
        adminToolsPage.clickOnButtonCreate();

        adminToolsPage.searchUser(login).shouldBe(present);
    }
    static protected AdminToolsPage deleteUser(String username)
    {
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        AdminToolsPage adminToolsPage = detailsPage.getMenu().openAdminTools();
        adminToolsPage.searchUser(username).shouldBe(present);
        adminToolsPage.clickOnUserName(username);
        adminToolsPage.clickOnButtonDeleteUser();
        return adminToolsPage;
    }
    protected void fillFieldsOnFormCreationContract(String valueContractWith, String kindOfDocument, String signatory,
     String performer, String documentDate, String agreementAmount, String vat,
     String numberOfAppendixPage, String numberPage, String summary,String node,
     String nameLegalEntity, String addressLegalEntity,String postAddressLegalEntity, String inn, String kpp,
     String nameContractor, String addressContractor, String postAddressContractor, String ceoNameContractor,
     String agreementSubjectCode, String agreementSubjectName,
     String paymentScheduleDate, String paymentScheduleAmount, String paymentScheduleType, String paymentScheduleDescription)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();

        contractCreatePage.setContractWith(valueContractWith);
        contractCreatePage.selectKindDocument(kindOfDocument);
        contractCreatePage.clickOnButtonGenerate();
        contractCreatePage.setDocumentDate(documentDate);
        contractCreatePage.setAgreementAmount(agreementAmount);
        contractCreatePage.setVAT(vat);
        contractCreatePage.setNumberOfAppendixPage(numberOfAppendixPage);
        contractCreatePage.setNumberPage(numberPage);
        contractCreatePage.setSummary(summary);
        contractCreatePage.setNode(node);
        contractCreatePage.selectSignatory(signatory).shouldHave(text(signatory));
        contractCreatePage.selectPerformer(performer).shouldHave(text(performer));

        createLegalEntity(nameLegalEntity, addressLegalEntity, postAddressLegalEntity, inn, kpp);
        createContractor(nameContractor, addressContractor, postAddressContractor, ceoNameContractor);
        createAgreementSubject(agreementSubjectCode, agreementSubjectName);
        createPaymentSchedule(paymentScheduleDate, paymentScheduleAmount, paymentScheduleType, paymentScheduleDescription);
    }
    protected void fillFieldsOnFormSupplementaryAgreement(String valueContractWith, String kindOfDocument, String signatory,
      String performer, String documentDate, String agreementAmount,
      String vat, String numberOfAppendixPage, String numberPage, String summary,String node,
      String nameLegalEntity, String addressLegalEntity,String postAddressLegalEntity, String inn, String kpp,
      String nameContractor, String addressContractor, String postAddressContractor, String ceoNameContractor,
      String agreementSubjectCode, String agreementSubjectName,
      String paymentScheduleDate, String paymentScheduleAmount, String paymentScheduleType, String paymentScheduleDescription)
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();

        supplementaryAgreementCreatePage.clickOnButtonMainAgreement();
        ContractCreatePage mainAgreement = supplementaryAgreementCreatePage.openMainContractCreatePage();
        fillFieldsOnFormCreationContract(valueContractWith, kindOfDocument, signatory, performer,documentDate,
                agreementAmount,vat,numberOfAppendixPage,numberPage,summary,node,
                nameLegalEntity,addressLegalEntity,postAddressLegalEntity,inn,kpp,
                nameContractor,addressContractor,postAddressContractor,ceoNameContractor,
                agreementSubjectCode,agreementSubjectName,
                paymentScheduleDate,paymentScheduleAmount,paymentScheduleType,paymentScheduleDescription);
        mainAgreement.clickOnButtonCreate();
        supplementaryAgreementCreatePage.selectMainAgreement();
        supplementaryAgreementCreatePage.clickOnButtonGenerate();
        supplementaryAgreementCreatePage.setDocumentDate(documentDate);
    }
    protected void createLegalEntity(String fullOrganisationName, String juridicalAddress, String postAddress, String inn, String kpp)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        LegalEntityCreatePage legalEntityCreatePage =  contractCreatePage.openCreatePageLegalEntity();
        legalEntityCreatePage.setFullOrganizationName(fullOrganisationName);
        legalEntityCreatePage.setJuridicalAddress(juridicalAddress);
        legalEntityCreatePage.setPostAddress(postAddress);
        legalEntityCreatePage.setINN(inn);
        legalEntityCreatePage.setKPP(kpp);
        legalEntityCreatePage.clickOnButtonCreate();
        contractCreatePage.setLegalEntity(fullOrganisationName).shouldHave(text(fullOrganisationName));
    }
    protected void createContractor(String fullOrganisationName, String juridicalAddress, String postAddress, String ceoName)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        ContractorCreatePage contractorCreatePage = contractCreatePage.openContractorCreatePage();
        contractorCreatePage.setFullOrganizationName(fullOrganisationName);
        contractorCreatePage.setJuridicalAddress(juridicalAddress);
        contractorCreatePage.setPostAddress(postAddress);
        contractorCreatePage.setCeoName(ceoName);
        contractorCreatePage.clickOnButtonCreate();
        contractCreatePage.setContractor(fullOrganisationName).shouldHave(text(fullOrganisationName));
    }
    protected void createAgreementSubject(String code, String name)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        AgreementSubjectCreatePage agreementSubject = contractCreatePage.openAgreementSubjectCreatePage();
        agreementSubject.setSubjectCode(code);
        agreementSubject.setSubjectName(name);
        agreementSubject.clickOnButtonCreate();
        contractCreatePage.selectAgreementSubject();
    }
    protected void createPaymentSchedule(String date, String amount, String type, String description)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        PaymentScheduleCreatePage paymentSchedule = contractCreatePage.openPaymentScheduleCreatePage();
        //paymentSchedule.setPlannedPaymentDate(date);
        paymentSchedule.setPaymentAmount(amount);
        paymentSchedule.selectTypePayment(type);
        paymentSchedule.setPaymentDescription(description);
        contractCreatePage.createPaymentSchedule();
    }
    protected DocumentDetailsPage openDocumentWithTask(String login, String pass, String nameContract)
    {
        DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
        LoginPage loginPage = new LoginPage();
        loginPage.inLoginAndPassword(login, pass);
        HomePage homePage = loginPage.clickOnLoginButton();
        homePage.getTableTasks().shouldBe(present);
        JournalsPage journalTask = homePage.openJournalTasks();
        sleep(20000);
        journalTask.refreshJournal();
        journalTask.openLinkDocument(nameContract);
        return documentDetailsPage;
    }
    protected void sendToApproval(String userName, String message)
    {
        DocumentDetailsPage documentDetailsPage = new DocumentDetailsPage();
        StartWorkflowPage startWorkflowPage = documentDetailsPage.openStartWorkflowPage();
        startWorkflowPage.setWorkflowDescription(message);
        startWorkflowPage.selectParticipant(userName);
        startWorkflowPage.clickOnButtonStartApproval();
    }
    @AfterClass
    static public void deleteUser()
    {
        PageBase pageBase = new PageBase();
        if (pageBase.getMenu().searchByText(userName).exists())
        {
            LoginPage loginPage = pageBase.getMenu().logOut(userName);
            loginPage.inLoginAndPassword(Settings.getLogin(),Settings.getPassword());
            loginPage.clickOnLoginButton();
        }
        AdminToolsPage adminToolsPage = deleteUser(userName);
        LoginPage loginPage = adminToolsPage.getMenu().logOut(UserNameAdmin);
        Assert.assertTrue(titleLoginPageRUS.equals(loginPage.getTitle()) || titleLoginPageEN.equals(loginPage.getTitle()));
    }
}
