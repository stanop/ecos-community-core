package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.*;
import ru.citeck.ecos.pages.createpages.*;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.sleep;

public class TestCreateContract extends SelenideTests{
    String userName = "Остап";
    String UserNameAdmin = "Administrator";
    @Test
    public void testForContract()
    {
        String login = "ostap";
        String pass = "ostap";
        String valueContractWith = "performer";
        String valueKindDocument = "Services";
        String message = "Согласуйте";

        createUser(userName, login, pass, "company_director");

        DocumentDetailsPage documentDetailsPage = createContract(valueContractWith, valueKindDocument, UserNameAdmin, userName);
        documentDetailsPage.getStatusDocument().shouldHave(text("New"));
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||
                "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));

        sendToApproval(userName, message);
        documentDetailsPage.getStatusDocument().shouldHave(text("On approval"));
        String nameContract = "Contract №"+documentDetailsPage.getNumberAgreement();

        documentDetailsPage.getMenu().logOut("Administrator");
        openDocumentWithTask(login, pass, nameContract);
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||
                          "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskConfirm(message);
        documentDetailsPage.getStatusDocument().shouldHave(text("On sign"));
        documentDetailsPage.getMenu().logOut(userName);

        openDocumentWithTask(Settings.getLogin(),Settings.getPassword(),nameContract);
        Assert.assertTrue("Citeck EcoS » Карточка".equals(documentDetailsPage.getTitle()) ||
                          "Citeck EcoS » Card details".equals(documentDetailsPage.getTitle()));
        documentDetailsPage.performTaskSign(message);
        documentDetailsPage.getStatusDocument().shouldHave(text("Active"));
        documentDetailsPage.clickOnActionMoveToArchive();
        sleep(15000);
        documentDetailsPage.getStatusDocument().shouldHave(text("Archive"));
    }
    @After
    public void deleteUser()
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
        Assert.assertTrue("Citeck EcoS » Войти".equals(loginPage.getTitle()) || "Citeck EcoS » Login".equals(loginPage.getTitle()));
    }

    private void createUser(String username, String login, String password, String group)
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
    private AdminToolsPage deleteUser(String username)
    {
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        AdminToolsPage adminToolsPage = detailsPage.getMenu().openAdminTools();
        adminToolsPage.searchUser(username).shouldBe(present);
        adminToolsPage.clickOnUserName(username);
        adminToolsPage.clickOnButtonDeleteUser();
        return adminToolsPage;
    }
    private DocumentDetailsPage createContract(String valueContractWith, String kindOfDocument, String signatory, String performer)
    {
        ContractCreatePage contractCreatePage = new ContractCreatePage();
        contractCreatePage.openCreatePageContract();

        contractCreatePage.setContractWith(valueContractWith);
        contractCreatePage.selectKindDocument(kindOfDocument);
        contractCreatePage.clickOnButtonGenerate();
        contractCreatePage.setDocumentDate("2016-07-22");
//        contractCreatePage.setDuration("2016-08-22");
        contractCreatePage.setAgreementAmount("50000");
        contractCreatePage.setVAT("10");
        contractCreatePage.setNumberOfAppendixPage("2");
        contractCreatePage.setNumberPage("7");
        contractCreatePage.setSummary("- Договор между физическими лицами на срок более одного года, а также, " +
                "если одной из сторон является юридическое лицо, должен быть заключен в письменной форме. " +
                "При аренде недвижимого имущества письменная форма также обязательна.\n" +
                "Если аренда предполагает выкуп имущества, т.е. перемену собственника, то форма договора аренды " +
                "должна соответствовать форме договора купли-продажи данного вида имущества.\n");
        contractCreatePage.setNode("Досрочное прекращение обязательства возможно при расторжении договора аренды. " +
                "В ст. 619 ГК РФ перечислены следующие основания, позволяющие арендодателю требовать расторжения " +
                "договора: использование имущества не по назначению, существенные нарушения условий договора; " +
                "значительное ухудшение арендатором арендованного имущества; несвоевременное внесение арендной платы " +
                "(более двух раз подряд по истечении установленного договором срока платежа); невыполнение " +
                "обязанности по проведению капитального ремонта арендованного имущества. Стороны могут предусмотреть" +
                " и иные основания расторжения договора. Применяя указанные положения, следует помнить, " +
                "что они являются специальными основаниями расторжения договора аренды.");
        contractCreatePage.selectSignatory(signatory).shouldHave(text(signatory));
        contractCreatePage.selectPerformer(performer).shouldHave(text(performer));

        createLegalEntity("ООО \"Старость\"", "Россия", "Россия", "80056479781430", "00688");
        createContractor("ИП Печкин", "Простоквашино д.1", "Простоквашино д.2", "Печкин ");
        createAgreementSubject("004", "Жилой дом");
        createPaymentSchedule("2016-07-27", "500", "rest", "нет");

        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        return documentDetailsPage;
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

}
