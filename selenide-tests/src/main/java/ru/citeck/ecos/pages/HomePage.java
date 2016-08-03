package ru.citeck.ecos.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class HomePage extends PageBase{

    public String getTitle()
    {
        return title();
    }
    public JournalsPage openJournalTasks()
    {
        $("[href *= \"journal-meta\"]").shouldBe(enabled).click();
        JournalsPage journalsPage = new JournalsPage();
        return  journalsPage;
    }
    public SelenideElement getTableTasks()
    {
        //return $("[id *= \"yuievtautoid\"]").shouldBe(present);
        return $("[id *= \"attributeswfmtaskType-liner\"]");
    }
}
