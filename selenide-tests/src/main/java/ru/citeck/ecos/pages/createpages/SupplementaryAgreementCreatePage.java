package  ru.citeck.ecos.pages.createpages;


import ru.citeck.ecos.Settings;
import static com.codeborne.selenide.Selenide.open;

public class SupplementaryAgreementCreatePage extends CreatePage {

    public void openCreatePage()
    {
        open("/node-create-page?type=contracts:supplementaryAgreement");
    }

}
