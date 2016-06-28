package ru.citeck.ecos.Tests;

import ru.citeck.ecos.pages.AdminToolsPage;
import ru.citeck.ecos.pages.HomePage;
import  ru.citeck.ecos.pages.SiteHomePage;
import org.junit.*;

public class TestHomePage extends SelenideTests {

    @Test
    public void successCreateSite()//test-case: EC.L1.004
    {
        String mySite = "mySite"+Math.random();
        HomePage homePage = new HomePage();
        //cancel create site
        homePage.openFormCreateSite();
        homePage.cancelCreateSiteFromHomePage();
        Assert.assertTrue("Alfresco » Домашняя страница".equals(homePage.getTitle()) || "Alfresco » User Dashboard".equals(homePage.getTitle()));
        //success create site
        homePage.openFormCreateSite();
        SiteHomePage siteHomePage = homePage.createSite(mySite);
        Assert.assertTrue("Alfresco » Главная страница сайта".equals(siteHomePage.getTitle()) || "Alfresco » Site Dashboard".equals(siteHomePage.getTitle()));
    }

    @Test
    public void successOpenAdminTools()
    {
        HomePage homePage = new HomePage();
        AdminToolsPage adminToolsPage = homePage.openAdminTools();
        Assert.assertTrue("Alfresco » Инструменты администратора".equals(adminToolsPage.getTitle()) || "Alfresco » Admin Tools".equals(adminToolsPage.getTitle()));
    }


}
