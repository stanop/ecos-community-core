<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale}" xml:lang="${locale}">
    <head>
        <meta http-equiv="Cache-Control" content="private" >
        <meta http-equiv="X-UA-Compatible" content="IE=Edge" />

        <title>
            <#if metaPage??>
                <#-- Title had been renamed by site manager in ui (value from site's dashboard page properties) -->
                <#assign pageTitle = metaPage.sitePageTitle!metaPage.title!""/>
            <#else>
                <#assign pageTitle = "">
                <#if page??>
                    <#-- Title defined in page's xml definition's <title> element -->
                    <#assign pageTitle = page.title!""/>
                    <#if page.titleId?? && msg(page.titleId) != page.titleId>
                         <#-- Page's xml definition had defined an i18n key -->
                         <#assign pageTitle = ((msg(page.titleId))!pageTitle)>
                    </#if>
                </#if>
                <#if context.properties["page-titleId"]??>
                     <#assign pageTitle = msg(context.properties["page-titleId"])>
                </#if>
            </#if>
            ${msg("page.title", pageTitle?html)}
        </title>

        <#assign webframeworkConfig = config.scoped["WebFramework"]["web-framework"]!>

        <script type="text/javascript">//<![CDATA[
            var appContext = "${url.context?js_string}";

            var dojoConfig = {
                baseUrl: "${url.context?js_string}${webframeworkConfig.dojoBaseUrl}",
                tlmSiblingOfDojo: false,
                locale: (navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage)).toLowerCase(),
                async: true,
                parseOnLoad: false,
                cacheBust: "${citeckUtils.getCacheBust()}",
                packages: [
                <#assign packages = webframeworkConfig.dojoPackages>
                <#list packages?keys as name>
                    { name: "${name}", location: "${packages[name]}"<#if webframeworkConfig.dojoPackagesMain[name]??>, main: "${webframeworkConfig.dojoPackagesMain[name]}"</#if>}<#if name_has_next>,</#if>
                </#list>
                ]
            };
        //]]></script>

        <script type="text/javascript" src="${url.context}${webframeworkConfig.dojoBootstrapFile}"></script>

        <#if hasReadPermissions>

            <#-- This MUST be placed before the <@outputJavaScript> directive to ensure that the Alfresco namespace
                 gets setup before any of the other Alfresco JavaScript dependencies try to make use of it. -->
            <@markup id="messages">
                <@generateMessages type="text/javascript" src="${url.context}/service/messages.js" locale="${locale}"/>
            </@markup>

            <@outputJavaScript/>
            <@outputCSS/>

            <#-- Common Resources -->
            <#include "/org/alfresco/components/head/resources.get.html.ftl" />
        <#else>
            <script type="text/javascript">
                window.location.href = "/share";
            </script>
        </#if>
    </head>

    <body id="Share" class="yui-skin-${theme} alfresco-share ${type!} claro">

        <div id="page-loading-root" class="loading-overlay" style="padding-top:100px">
            <div class="loading-container">
                <div class="loading-indicator"></div>
            </div>
        </div>
        <div id="page-content-root" style="display: none;">
            <div id="card-details-root"></div>
        </div>

        <#if hasReadPermissions>

            <#assign pageArgsMap = ((page.url.templateArgs!{}) + (page.url.args!{}) + {
                "pageid": "card-details",
                "theme": "${theme!}"
            }) />

            <@relocateJavaScript/>

            <script type="text/javascript">//<![CDATA[

                require(['js/citeck/modules/page/card-details/card-details'], function(components) {
                    components.renderPage('card-details-root', {
                        alfescoUrl: window.location.protocol + "//" + window.location.host + "${url.context?js_string}/proxy/alfresco/",
                        pageArgs: {
                            <#list pageArgsMap?keys as argKey>
                                "${argKey}":"${pageArgsMap[argKey]!}"<#if argKey_has_next>,</#if>
                            </#list>
                        },
                        userName: "${((user.name)!"")?js_string}",
                        nodeBaseInfo: ${nodeBaseInfoStr}
                    });
                });

                setTimeout(function() {
                    require(['jquery'], function() {
                        $("#page-loading-root").fadeOut("slow", function(){
                            $("#page-content-root").fadeIn("slow");
                        });
                    });
                }, 2000);

                <#assign tabletCSS><@checksumResource src="${url.context}/res/css/tablet.css"/></#assign>
                <!-- Android & iPad CSS overrides -->
                if (navigator.userAgent.indexOf(" Android ") !== -1 || navigator.userAgent.indexOf("iPad;") !== -1 || navigator.userAgent.indexOf("iPhone;") !== -1 ) {
                    document.write("<link media='only screen and (max-device-width: 1024px)' rel='stylesheet' type='text/css' href='${tabletCSS}'/>");
                    document.write("<link rel='stylesheet' type='text/css' href='${tabletCSS}'/>");
                }
            //]]></script>

            <div id="alfresco-yuiloader"></div>

            <script type="text/javascript">//<![CDATA[
                Alfresco.util.YUILoaderHelper.loadComponents(true);
                <#-- Security - ensure user has a currently authenticated Session when viewing a user auth page e.g. when Back button is used -->
                <#if page?? && (page.authentication="user" || page.authentication="admin")>
                    Alfresco.util.Ajax.jsonGet({
                        url: Alfresco.constants.URL_CONTEXT + "service/modules/authenticated?noCache=" + new Date().getTime() + "&a=${page.authentication?html}"
                    });
                </#if>
            //]]></script>
        </#if>
    </body>
</html>