package ru.citeck.ecos.Tests;

import com.codeborne.selenide.SelenideElement;
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
        contractCreatePage.setAgreementNumber();
        contractCreatePage.setContractDate();
        contractCreatePage.selectCurrency();
        contractCreatePage.setSummary();
        //contractCreatePage.setContractValue();//bug ECOSCOM-453
        //contractCreatePage.setVAT();
        //contractCreatePage.setNumberOfAppendixPage();
        //contractCreatePage.setNumberPage();
        //contractCreatePage.createPaymentSchedule();

        CreatePage createPage = new CreatePage();
        SelenideElement selenideElement = createPage.clickOnResetButton();
        selenideElement.shouldBe(visible);

        contractCreatePage.setLegalEntityFormCreateContract();
        contractCreatePage.setContractorFormCreateContract();
        contractCreatePage.setContractWith();
        contractCreatePage.setAgreementNumber();
        contractCreatePage.setContractDate();
        contractCreatePage.selectCurrency();

        DocumentDetailsPage documentDetailsPage = contractCreatePage.clickOnCreateContentButton();
        Assert.assertTrue("Alfresco » Сведения о документе".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Card details".equals(documentDetailsPage.getTitle()));
        //Assert.assertTrue("Alfresco » Карточка".equals(documentDetailsPage.getTitle()) ||  "Alfresco » Card details".equals(documentDetailsPage.getTitle()));
    }
    @Test
    public void createSupplementaryAgreement()
    {
        SupplementaryAgreementCreatePage supplementaryAgreementCreatePage = new SupplementaryAgreementCreatePage();
        supplementaryAgreementCreatePage.openCreatePage();
    }
}
