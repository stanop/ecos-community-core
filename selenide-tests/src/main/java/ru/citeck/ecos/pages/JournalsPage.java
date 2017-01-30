package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class JournalsPage {

    public String getTitle() {
        return title();
    }

    public SelenideElement getTable() {
        return $("#yui-main table");
    }

    public void refreshJournal() {
        $("[data-bind = \"click: performSearch\"]").shouldBe(enabled).click();
    }

    public SelenideElement getNameContract(String nameContract) {
        return $(byText(nameContract));
    }

    public DocumentDetailsPage openLinkDocument(String nameContract) {
        $(byText(nameContract)).shouldBe(present).click();
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        return detailsPage;
    }
}
