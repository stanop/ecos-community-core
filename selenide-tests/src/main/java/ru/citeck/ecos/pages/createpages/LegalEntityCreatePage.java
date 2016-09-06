package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class LegalEntityCreatePage extends LegalEntityCreatePageBase{

    public void setINN(String value) {
        $("[id *= \"body-idocs_inn\"]").shouldBe(present).setValue(value);
    }

    public void setKPP(String value) {
        $("[id *= \"body-idocs_kpp\"]").shouldBe(present).setValue(value);
    }
}
