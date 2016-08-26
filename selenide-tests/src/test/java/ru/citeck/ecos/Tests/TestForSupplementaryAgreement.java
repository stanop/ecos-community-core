package ru.citeck.ecos.Tests;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import ru.citeck.ecos.Settings;
import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.DocumentDetailsPage;
import ru.citeck.ecos.pages.LoginPage;
import ru.citeck.ecos.pages.PageBase;
import ru.citeck.ecos.pages.createpages.SupplementaryAgreementCreatePage;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.sleep;

public class TestForSupplementaryAgreement extends ContractsModuleTestBase{
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
    private String message = "Согласуйте доп. соглашение";
    @Test
    public void testCreateSupplementaryAgreement()
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePageSupplementaryAgreement();
        fillFieldsOnFormSupplementaryAgreement(valueContractWith, valueKindDocument, UserNameAdmin, userName, documentDate,
                agreementAmount, vat, numberOfAppendixPage, numberPage, summary, node,
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
        String nameContract = "Add'l agreement №"+documentDetailsPage.getNumberSupplementaryAgreement();

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
}
