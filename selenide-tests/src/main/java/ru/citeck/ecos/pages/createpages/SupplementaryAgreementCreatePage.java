package  ru.citeck.ecos.pages.createpages;


import ru.citeck.ecos.Settings;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SupplementaryAgreementCreatePage extends CreatePage {

    public void openCreatePage()
    {
        open("/node-create-page?type=contracts:supplementaryAgreement");
    }

    public void selectMainAgreement()
    {
        $("[id *= \"contracts_mainAgreement-journalControl-button\"]").shouldBe(present).click();
        $("[id *= \"workspace\"]").shouldBe(present).click();
        $("[id *= \"contracts_mainAgreement-journalControl-journalPanel-selectedElementsTable\"] table tbody").shouldBe(present);
        $("[id *= \"mainAgreement-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }

}
