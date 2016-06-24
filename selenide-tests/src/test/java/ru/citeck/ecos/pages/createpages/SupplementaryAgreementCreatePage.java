package  ru.citeck.ecos.pages.createpages;


import ru.citeck.ecos.Settings;
import static com.codeborne.selenide.Selenide.open;

public class SupplementaryAgreementCreatePage {

    public void openCreatePage()
    {
        open(Settings.getBaseURL()+"/node-create-page?type=contracts:supplementaryAgreement");
    }
}
