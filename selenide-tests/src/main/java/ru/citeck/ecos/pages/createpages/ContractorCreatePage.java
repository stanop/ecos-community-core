package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class ContractorCreatePage extends LegalEntityCreatePageBase{

    public void setCeoName(String value)
    {
        $("[id *= \"body-idocs_CEOname\"]").shouldBe(present).setValue(value);
    }
}
