package ru.citeck.ecos.pages;

import ru.citeck.ecos.pages.createpages.CreatePageBase;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class JournalsPage {


//    public void openMainJournal()
//    {
//        open("/journals2/list/main");
//
//    }
    public String getTitle()
    {
        return title();
    }


//    public void clickOnJournal(int index)
//    {
//        $$(".items-list > ul > li > span").get(index).shouldBe(present).click();
//        //$(By.cssSelector("span:contains(\"Content\")")).shouldBe(present).click();
//        //$(By.xpath(".//*[text() = \"Content\"]/..")).shouldBe(present).click();
//        $(By.cssSelector("#yui-main")).shouldBe(present);
//    }
    public SelenideElement getTable()
    {
        return $("#yui-main table");
    }
//    public CreatePageBase clickOnButtonCreate()
//    {
//        CreatePageBase createPage = new CreatePageBase();
//        $("#alf-content > div > span > span > button").shouldBe(present).click();
//        $("#alf-content [href *= \"create-content\"]").shouldBe(present).click();
//        return createPage;
//    }
    public void refreshJournal()
    {
        $("[data-bind = \"click: performSearch\"]").shouldBe(enabled).click();
    }
//    public void sortDescendingAttributeDocument()
//    {
//        $("[href *= \"href-attributeswfmdocument\"]").shouldBe(enabled).click();
//    }
    public DocumentDetailsPage openLinkDocument(String nameContract)
    {
        $(byText(nameContract)).shouldBe(present).click();
        DocumentDetailsPage detailsPage = new DocumentDetailsPage();
        return detailsPage;
    }
}
