package ru.citeck.ecos.pages.createpages;

import static com.codeborne.selenide.Condition.present;
import static com.codeborne.selenide.Selenide.$;

public class ProductsAndServicesCreatePage extends CreatePageBase{

    public void setTitleProductOrService(String title) {
        $("[id *= \"body-cm_title\"]").shouldBe(present).setValue(title);
    }

    public void selectType(String type) {
        $("[id *= \"body-pas_type\"]").shouldBe(present).selectOptionByValue(type);
    }

    public void selectUnit(String unit) {
        $("[id *= \"body-pas_entityUnit\"]").shouldBe(present).selectOption(unit);
    }

    public void selectCurrency(String currency) {
        $("[id *= \"pas_currency\"]").shouldBe(present).selectOption(currency);
    }

    public void selectProductOrService() {
        $("[id *= \"containsOriginalProductsAndServices-journalControl-journalPanel-selectedElementsTable\"] table tbody td").shouldBe(present);
        $("[id *= \"containsOriginalProductsAndServices-journalControl-journalPanel-submitInput\"]").shouldBe(present).click();
    }
}
