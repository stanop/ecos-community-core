<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/header-community/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/header/header-tokens.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">

// ---------------------
// HEADER MENU
// ---------------------

var footer = findObjectById(model.jsonModel.widgets, "ALF_STICKY_FOOTER"),
    currentSite = page.url.templateArgs.site || getLastSiteFromCookie();

if (footer && footer.config.widgetsForFooter) {
    footer.config.widgetsForFooter = [];
    footer.config.widgetsForFooter.push({
        id: "ALF_SHARE_FOOTER",
        name: "alfresco/footer/AlfShareFooter",
        config: {
            semanticWrapper: "footer",
            templateString: '<div class="footer footer-com"> <div class="footer-content"> <a href="http://citeck.ru/" class="contact-us-link">Контактная информация</a><div class="description">Citeck EcoS на платформе Alfresco™</div></div></div>'
        }
    });
}

model.__alf_current_site__ = currentSite;