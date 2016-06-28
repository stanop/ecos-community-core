package ru.citeck.ecos.pages;

import  ru.citeck.ecos.pages.createpages.CreatePage;
import  ru.citeck.ecos.Settings;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class JournalsPage {


    public void openMainJournal()
    {
        open(Settings.getBaseURL()+"/journals2/list/main");

    }
    public String getTitle()
    {
        return title();
    }


    public void clickOnJournal(int index)
    {
        $$(By.cssSelector(".items-list > ul > li > span")).get(index).shouldBe(present).click();
        //$(By.cssSelector("span:contains(\"Content\")")).shouldBe(present).click();
        //$(By.xpath(".//*[text() = \"Content\"]/..")).shouldBe(present).click();
        $(By.cssSelector("#yui-main")).shouldBe(present);
    }
    public SelenideElement getTable()
    {
        return $(By.cssSelector("#yui-main table"));
    }
    public CreatePage clickOnButtonCreate()
    {
        CreatePage createPage = new CreatePage();
        $(By.cssSelector("#alf-content > div > span > span > button")).click();
        $(By.cssSelector("#alf-content [href *= \"create-content\"]")).shouldBe(present).click();
        return createPage;
    }

    public SelenideElement clickOnFilter()
    {
        $(By.cssSelector(".filter button")).shouldBe(present).click();
        return $(By.cssSelector("[id *= default-criteria-buttons]"));
    }
    public void addCriterionNameContainsValue(/*передаю значение для setValue*/)
    {
        $$(By.cssSelector("[id *= \"default-criteria-buttons\"] button")).get(3).shouldBe(present).click();//клик на кнопку "Добавить критерий"
        $(By.cssSelector("[id *= \"default-add-criterion-menu\"] [index=\"0\"]")).shouldBe(present).click();//выбор критерия "Имя" для журнала контент
        $(By.cssSelector("[id *= \"default-filter-criteria\"] [id*=name]")).shouldBe(present).setValue("link");//заполнение критерия
        $(By.cssSelector(".apply [data-bind *= \"applyCriteria\"]")).shouldBe(present).click();// клик на кнопку "Применить фильтр"
        //$$(By.cssSelector("[tabindex = \"0\"] > tr")).shouldHaveSize(4);
    }
    public void clearFilter()
    {
        $(By.cssSelector("[data-bind *= \"clearCriteria\"]")).shouldBe(present).click();
       // $$(By.cssSelector("[tabindex = \"0\"] > tr")).shouldHaveSize(10);
    }
    public void clickOnSaveFilter()
    {
        $(By.cssSelector("[data-bind *= \"saveFilter\"]")).shouldBe(present).click();
    }
    public void saveFilter()
    {
        $(By.cssSelector("#userInput input")).setValue("filter"+Math.random()).shouldBe(present);
        $(By.cssSelector(".yui-button.yui-push-button.default button")).shouldBe(present).click();
    }
    public void cancelSaveFilter()
    {
        $$(By.cssSelector("#userInput button")).get(1).shouldBe(present).click();
    }
    public SelenideElement getSavedFilter()
    {
        return $(By.cssSelector("[data-bind *= \"root.filter\"][class = \"selected\"]")).shouldBe(present);
    }

    public SelenideElement clickOnSettings()
    {
        $(By.cssSelector(".settings button")).shouldBe(present).click();
        return $(By.cssSelector("[id *= \"toolbar-settings\"]"));
    }
    public void clickOnCheckboxSettings(int index)
    {
        $$(By.cssSelector("[type = \"checkbox\"]")).get(index).shouldBe(present).click();
    }
    public  void clickOnApplyButton()
    {
        $(By.cssSelector("[data-bind *= \"applySettings\"]")).shouldBe(present).click();
        //return $$(By.cssSelector("#yuievtautoid-0 thead th"));
    }
    public void clickClearSettings()
    {
        $(By.cssSelector("[data-bind *= \"resetSettings\"]")).shouldBe(present).click();
    }
    public void clickOnSaveSettings()
    {
        $(By.cssSelector("[data-bind *= \"saveSettings\"]")).click();
    }
    public void setNameSettings()
    {
        $(By.cssSelector("#userInput input")).setValue("settings"+Math.random()).shouldBe(present);
    }
    public void saveSettings()
    {
        $(By.cssSelector(".yui-button.yui-push-button.default button")).shouldBe(present).click();
    }
    public void cancelSaveSettings()
    {
        $$(By.cssSelector("#userInput button")).get(1).shouldBe(present).click();
    }
    public SelenideElement getSavedSettings()
    {
        return $(By.cssSelector("[data-bind *= 'root.settings'][class = 'selected']")).shouldBe(present);
    }

}
