package ru.citeck.ecos.pages.homepagessites;

import ru.citeck.ecos.pages.PageBase;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.title;

public class HomePageSiteContracts extends PageBase{
    public String getTitle()
    {
        return title();
    }
    public void openCreateFormContracts()
    {
        $("[id *= \"create-variants-button\"]").shouldBe(present).click();
        $("a[href *= \"agreement\"]").shouldBe(present).click();
    }
    public void openCreateFormSupplementaryAgreement()
    {
        $("[id *= \"create-variants-button\"]").shouldBe(present).click();
        $("a[href *= \"supplementaryAgreement\"]").shouldBe(present).click();
    }
    public void openCreateFormPayment()
    {
        $("[id *= \"create-variants-button\"]").shouldBe(present).click();
        $("a[href *= \"payment\"]").shouldBe(present).click();
    }
    public void openCreateFormClosingDocument()
    {
        $("[id *= \"create-variants-button\"]").shouldBe(present).click();
        $("a[href *= \"closingDocument\"]").shouldBe(present).click();
    }


}
