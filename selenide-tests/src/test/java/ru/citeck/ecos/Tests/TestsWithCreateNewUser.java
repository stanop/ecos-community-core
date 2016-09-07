package ru.citeck.ecos.Tests;

import org.junit.*;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.*;
import ru.citeck.ecos.pages.createpages.*;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.sleep;

public class TestsWithCreateNewUser extends ContractsModuleTestBase{
    static private String titleLoginPageRUS = "Citeck EcoS » Войти";
    static private String titleLoginPageEN = "Citeck EcoS » Login";

    static private String userName = "TestUser";
    static private String login = "user1";
    static private  String pass = "user1";
    static private  String group = "company_director";
    static private String UserNameAdmin = "Administrator";

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

    private String nameBaseDocument = "Contract";
    private String paymentFor = "client";
    private String titleProductOrService = "TestService";
    private String typeProductOrService = "service";
    private String unit = "шт";
    private String currency = "Рубль";
    private String statusDraft = "Draft";
    private String statusOnPayment = "On payment";
    private String statusPaid = "Paid";

    private String nameOriginalLocation = "В архиве";
    private String typeClosingDocument = "Act";

    @BeforeClass
    static public void createUser() {
        createUser(userName, login, pass, group);
    }

    @Before
    public void openSiteContract() {
        HomePage homePage = new HomePage();
        homePage.openSiteContract();
    }

    @Test
    public void testForContract() {
        ContractCreatePage contractCreatePage =  new ContractCreatePage();
        contractCreatePage.openCreatePageContract();
        fillFieldsOnFormCreationContract(valueContractWith, valueKindDocument, UserNameAdmin, userName, documentDate,
                agreementAmount,vat,numberOfAppendixPage,numberPage,summary,node,
                nameLegalEntity,addressLegalEntity,postAddressLegalEntity,inn,kpp,
                nameContractor,addressContractor,postAddressContractor,ceoNameContractor,
                agreementSubjectCode,agreementSubjectName,
                paymentScheduleDate,paymentScheduleAmount,paymentScheduleType,paymentScheduleDescription);
        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusNew));
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));

        sendToApproval(userName, message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnApproval));
        String nameContract = "Contract №" + documentDetailsPage.getNumberAgreement();

        documentDetailsPage.getMenu().logOut();
        openDocumentWithTask(login, pass, nameContract);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                          titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskConfirm(message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnSign));
        documentDetailsPage.getMenu().logOut();

        openDocumentWithTask(Settings.getLogin(),Settings.getPassword(),nameContract);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                          titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskSign();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusActive));
        documentDetailsPage.clickOnActionMoveToArchive();
        sleep(15000);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusArchive));
    }

    @Test
    public void testForSupplementaryAgreement() {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePageSupplementaryAgreement();
        fillFieldsOnFormSupplementaryAgreement(valueContractWith, valueKindDocument, UserNameAdmin, userName,
                documentDate,agreementAmount, vat, numberOfAppendixPage, numberPage, summary, node,
                nameLegalEntity, addressLegalEntity, postAddressLegalEntity, inn, kpp,
                nameContractor, addressContractor, postAddressContractor, ceoNameContractor,
                agreementSubjectCode, agreementSubjectName,
                paymentScheduleDate, paymentScheduleAmount, paymentScheduleType, paymentScheduleDescription);
        DocumentDetailsPage documentDetailsPage = supplementaryAgreementCreatePage.clickOnCreateContentButton();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusNew));
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));

        sendToApproval(userName, message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnApproval));
        String nameSupplementaryAgreement = "Add'l agreement №" + documentDetailsPage.getNumberSupplementaryAgreement();

        documentDetailsPage.getMenu().logOut();
        openDocumentWithTask(login, pass, nameSupplementaryAgreement);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskConfirm(message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnSign));
        documentDetailsPage.getMenu().logOut();

        openDocumentWithTask(Settings.getLogin(),Settings.getPassword(),nameSupplementaryAgreement);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskSign();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusActive));
        documentDetailsPage.clickOnActionMoveToArchive();
        sleep(15000);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusArchive));
    }

    @Test
    public void testForPayment() {
        PaymentCreatePage paymentCreatePage = new PaymentCreatePage();
        paymentCreatePage.openCreatePagePayment();
        fillFieldsOnFormPayment(nameBaseDocument,valueContractWith, valueKindDocument, UserNameAdmin, userName,
                documentDate, agreementAmount, vat, numberOfAppendixPage, numberPage, summary, node,
                nameLegalEntity, addressLegalEntity, postAddressLegalEntity, inn, kpp,
                nameContractor, addressContractor, postAddressContractor, ceoNameContractor,
                agreementSubjectCode, agreementSubjectName,
                paymentScheduleDate, paymentScheduleAmount, paymentScheduleType, paymentScheduleDescription,
                paymentFor, titleProductOrService,typeProductOrService,unit,currency);
        DocumentDetailsPage documentDetailsPage = paymentCreatePage.clickOnCreateContentButton();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusDraft));
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));

        sendToApproval(userName, message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnApproval));
        String namePayment = "Invoice №" + documentDetailsPage.getNumberPayment() + " for " + nameContractor;

        documentDetailsPage.getMenu().logOut();
        openDocumentWithTask(login, pass, namePayment);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskConfirm(message);
        documentDetailsPage.getStatusDocument().shouldHave(text(statusOnPayment));
        documentDetailsPage.getMenu().logOut();

        openDocumentWithTask(Settings.getLogin(),Settings.getPassword(),namePayment);
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskAffirm();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusPaid));
    }

    @Test
    public void testForClosingDocument()
    {
        ClosingDocumentCreatePage closingDocumentCreatePage = new ClosingDocumentCreatePage();
        closingDocumentCreatePage.openCreatePageClosingDocument();
        fillFieldsOnFormCreateClosingDocument(nameBaseDocument,valueContractWith, valueKindDocument, UserNameAdmin,
                userName, documentDate, agreementAmount, vat, numberOfAppendixPage, numberPage, summary, node,
                nameLegalEntity, addressLegalEntity, postAddressLegalEntity, inn, kpp,
                nameContractor, addressContractor, postAddressContractor, ceoNameContractor,
                agreementSubjectCode, agreementSubjectName,
                paymentScheduleDate, paymentScheduleAmount, paymentScheduleType, paymentScheduleDescription, paymentFor,
                titleProductOrService,typeProductOrService,unit,currency, nameOriginalLocation, typeClosingDocument);
        DocumentDetailsPage documentDetailsPage = closingDocumentCreatePage.clickOnCreateContentButton();
        documentDetailsPage.getStatusDocument().shouldHave(text(statusNew));
        Assert.assertTrue(titleCardDetailsRUS.equals(documentDetailsPage.getTitle()) ||
                titleCardDetailsEN.equals(documentDetailsPage.getTitle()));
    }

    @AfterClass
    static public void deleteUser() {
        AdminToolsPage adminToolsPage = new AdminToolsPage();
        LoginPage loginPage = adminToolsPage.getMenu().logOut();
        Assert.assertTrue(titleLoginPageRUS.equals(loginPage.getTitle())
                || titleLoginPageEN.equals(loginPage.getTitle()));

        loginPage.inLoginAndPassword(Settings.getLogin(), Settings.getPassword());
        loginPage.clickOnLoginButton();

        adminToolsPage = deleteUser(userName);

        loginPage  = adminToolsPage.getMenu().logOut();
        Assert.assertTrue(titleLoginPageRUS.equals(loginPage.getTitle())
                || titleLoginPageEN.equals(loginPage.getTitle()));
    }
}
