package ru.citeck.ecos.pages.createpages;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class ContractCreatePage extends CreatePage{

    public void  openNewCreatePage()
    {
        open("/node-create-page?type=contracts:agreement");
    }
    public void setContractWith()
    {
        $(By.cssSelector("[id *= \"contracts_contractWith\"]")).selectOptionByValue("client");
    }

    public void setContractValue()
    {
        $(By.cssSelector("[id *= \"contracts_agreementAmount\"]")).setValue("2000");
    }
    public void setVAT()
    {
        $(By.cssSelector("[id *= \"contracts_VAT\"]")).setValue("2018");
    }
    public void setSummary()
    {
        $(By.cssSelector("[id *= \"idocs_summary\"]")).setValue("Summary");
    }
    public void setNumberOfAppendixPage()
    {
        $(By.cssSelector("[id *= \"idocs_appendixPagesNumber\"]")).setValue("2");
    }
    public void setNumberPage()
    {
        $(By.cssSelector("[id *= \"idocs_pagesNumber\"]")).setValue("1");
    }
    public void createPaymentSchedule()
    {
        $(By.cssSelector("[id *= \"payments_payments-createObjectControl\"] button")).shouldBe(present).click();
        $(By.cssSelector("[id *= \"payments_paymentAmount\"]")).setValue("2000");
        $(By.cssSelector("[id *= \"form-submit\"]")).shouldBe(present).click();
    }
}
