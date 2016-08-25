package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
import org.junit.*;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.*;
import ru.citeck.ecos.pages.createpages.*;

import javax.print.Doc;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.sleep;

public class TestCreateContract extends SelenideTests{
    static private String titleLoginPageRUS = "Citeck EcoS » Войти";
    static private String titleLoginPageEN = "Citeck EcoS » Login";
    private String titleCardDetailsRUS = "Citeck EcoS » Карточка";
    private String titleCardDetailsEN = "Citeck EcoS » Card details";
    private String statusNew = "New";
    private String statusOnApproval = "On approval";
    private String statusOnSign = "On sign";
    private String statusActive = "Active";
    private String statusArchive = "Archive";

    static private String userName = "Остап";
    static private String login = "ostap";
    static private String pass = "ostap";
    static private String group = "company_director";
    static private String UserNameAdmin = "Administrator";
    private String loginAdmin = "Administrator";

    private String valueContractWith = "performer";
    private String valueKindDocument = "Services";
    private String documentDate = "2016-08-25";
    private String agreementAmount = "50000";
    private String vat = "10";
    private String numberOfAppendixPage = "2";
    private String numberPage = "7";
    private String summary = "- Договор между физическими лицами на срок более одного года, а также, " +
            "если одной из сторон является юридическое лицо, должен быть заключен в письменной форме. " +
            "При аренде недвижимого имущества письменная форма также обязательна.\n" +
            "Если аренда предполагает выкуп имущества, т.е. перемену собственника, то форма договора аренды " +
            "должна соответствовать форме договора купли-продажи данного вида имущества.\n";
    private String node = "Досрочное прекращение обязательства возможно при расторжении договора аренды. " +
            "В ст. 619 ГК РФ перечислены следующие основания, позволяющие арендодателю требовать расторжения " +
            "договора: использование имущества не по назначению, существенные нарушения условий договора; " +
            "значительное ухудшение арендатором арендованного имущества; несвоевременное внесение арендной платы " +
            "(более двух раз подряд по истечении установленного договором срока платежа); невыполнение " +
            "обязанности по проведению капитального ремонта арендованного имущества. Стороны могут предусмотреть" +
            " и иные основания расторжения договора. Применяя указанные положения, следует помнить, " +
            "что они являются специальными основаниями расторжения договора аренды.";

    private String nameLegalEntity = "ООО \"Зеленоглазое такси\"";
    private String addressLegalEntity = "Россия";
    private String postAddressLegalEntity = "Россия";
    private String inn = "80056479781430";
    private String kpp = "00688";

    private String nameContractor = "ООО Империал";
    private String addressContractor = "ул. Мира д.1";
    private String postAddressContractor = "Ул. Мира д.2";
    private String ceoNameContractor = "Империал";

    private String agreementSubjectCode = "004";
    private String agreementSubjectName = "Жилой дом";

    private String paymentScheduleDate = "2016-07-27";
    private String paymentScheduleAmount = "500";
    private String paymentScheduleType = "rest";
    private String paymentScheduleDescription = "нет";

    private String message = "Согласуйте";
    @BeforeClass
    static public void createUser()
    {
        createUser(userName, login, pass, group);
    }
    @Test
    public void testCreateContract()
    {
        ContractCreatePage contractCreatePage =  new ContractCreatePage();
        contractCreatePage.openCreatePageContract();
        fillFieldsOnFormCreationContract(valueContractWith, valueKindDocument, UserNameAdmin, userName);
        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusNew));
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));

        sendToApproval(userName, message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnApproval));
        String nameContract = "Contract №"+documentDetailsPage.getNumberAgreement();

        documentDetailsPage.getMenu().logOut(loginAdmin);
        openDocumentWithTask(login, pass, nameContract);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                          titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskConfirm(message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnSign));
        documentDetailsPage.getMenu().logOut(userName);

        openDocumentWithTask(Settings.getLogin(),Settings.getPassword(),nameContract);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                          titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskSign(message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusActive));
        documentDetailsPage.clickOnActionMoveToArchive();
        sleep(15000);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusArchive));
    }
    @Test
    public void testCreateSupplementaryAgreement()
    {
        DocumentDetailsPage documentDetailsPage = createSupplementaryAgreement();
    }
    static private void createUser(String username, String login, String password, String group)
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
    static private AdminToolsPage deleteUser(String username)
    {
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        AdminToolsPage adminToolsPage = detailsPage.getMenu().openAdminTools();
        adminToolsPage.searchUser(username).shouldBe(present);
        adminToolsPage.clickOnUserName(username);
        adminToolsPage.clickOnButtonDeleteUser();
        return adminToolsPage;
    }
    private void fillFieldsOnFormCreationContract(String valueContractWith, String kindOfDocument, String signatory, String performer)
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
    private DocumentDetailsPage createSupplementaryAgreement()
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePageSupplementaryAgreement();
        supplementaryAgreementCreatePage.clickOnButtonMainAgreement();
        ContractCreatePage mainAgreement = supplementaryAgreementCreatePage.openMainContractCreatePage();
        fillFieldsOnFormCreationContract(valueContractWith, valueKindDocument, UserNameAdmin, userName);
        mainAgreement.clickOnButtonCreate();
        supplementaryAgreementCreatePage.selectMainAgreement();
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        return detailsPage;
    }
    private void createLegalEntity(String fullOrganisationName, String juridicalAddress, String postAddress, String inn, String kpp)
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
    private void createContractor(String fullOrganisationName, String juridicalAddress, String postAddress, String ceoName)
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
    private void createAgreementSubject(String code, String name)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        AgreementSubjectCreatePage agreementSubject = contractCreatePage.openAgreementSubjectCreatePage();
        agreementSubject.setSubjectCode(code);
        agreementSubject.setSubjectName(name);
        agreementSubject.clickOnButtonCreate();
        contractCreatePage.selectAgreementSubject();
    }
    private void createPaymentSchedule(String date, String amount, String type, String description)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        PaymentScheduleCreatePage paymentSchedule = contractCreatePage.openPaymentScheduleCreatePage();
        //paymentSchedule.setPlannedPaymentDate(date);
        paymentSchedule.setPaymentAmount(amount);
        paymentSchedule.selectTypePayment(type);
        paymentSchedule.setPaymentDescription(description);
        contractCreatePage.createPaymentSchedule();
    }
    private DocumentDetailsPage openDocumentWithTask(String login, String pass, String nameContract)
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
    private void sendToApproval(String userName, String message)
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
