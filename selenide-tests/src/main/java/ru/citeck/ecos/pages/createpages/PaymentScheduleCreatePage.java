package ru.citeck.ecos.pages.createpages;


import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class PaymentScheduleCreatePage {
    public void setPlannedPaymentDate(String date)
    {
        $("[id *= \"body-payments_plannedPaymentDate\"]").shouldBe(present).setValue(date);
    }
    public void setPaymentAmount(String value)
    {
        $("[id *= \"body-payments_paymentAmount\"]").shouldBe(present).setValue(value);
    }
    public void selectTypePayment(String value)
    {
        $("[id *= \"body-payments_typePayment\"]").shouldBe(present).selectOptionByValue(value);
    }
    public void setPaymentDescription(String description)
    {
        $("[id *= \"body-payments_description\"]").shouldBe(present).setValue(description);
    }
}
