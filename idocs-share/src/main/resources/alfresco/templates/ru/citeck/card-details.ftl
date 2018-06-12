<#import "/org/alfresco/import/alfresco-common.ftl" as common />

<#assign DEBUG = (common.globalConfig("client-debug", "false") = "true")>
<#assign AUTOLOGGING = (common.globalConfig("client-debug-autologging", "false") = "true")>
<#assign theme = (page.url.args.theme!theme)?html />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale}" xml:lang="${locale}">
    <head>
        <meta http-equiv="Cache-Control" content="private" >
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
        <meta http-equiv="X-UA-Compatible" content="IE=Edge" />

        <#if hasReadPermissions>

            <#-- This MUST be placed before the <@outputJavaScript> directive to ensure that the Alfresco namespace
                 gets setup before any of the other Alfresco JavaScript dependencies try to make use of it. -->
            <@markup id="messages">
                <#-- Common i18n msg properties -->
                <@generateMessages type="text/javascript" src="${url.context}/service/messages.js" locale="${locale}"/>
            </@markup>

            <#-- Component Resources from .get.head.ftl files or from dependency directives processed before the
                <@outputJavaScript> and <@outputCSS> directives. -->
            ${head}

            <#-- This is where the JavaScript and CSS dependencies will initially be added through the use of the
                 <@script> and <@link> directives. The JavaScript can be moved through the use
                 of the <@relocateJavaScript> directive (i.e. to move it to the end of the page). These directives
                 must be placed before directives that add dependencies to them otherwise those resources will
                 be placed in the output of the ${head} variable (i.e. this applied to all usage of those directives
                 in *.head.ftl files) -->
            <@outputJavaScript/>
            <@outputCSS/>

            <#list pageDependencies.css as cssDep>
                <@link rel="stylesheet" type="text/css" href="${url.context}/res/${cssDep}.css" />
            </#list>

            <#-- Common Resources -->
            <#include "/org/alfresco/components/head/resources.get.html.ftl" />

            <#include "/org/alfresco/components/form/form.js.ftl"/>
            <#include "/org/alfresco/components/form/form.css.ftl"/>

            <@markup id="ipadStylesheets">
                <#assign tabletCSS><@checksumResource src="${url.context}/res/css/tablet.css"/></#assign>
                <!-- Android & iPad CSS overrides -->
                <script type="text/javascript">
                    if (navigator.userAgent.indexOf(" Android ") !== -1 || navigator.userAgent.indexOf("iPad;") !== -1 || navigator.userAgent.indexOf("iPhone;") !== -1 )
                    {
                        document.write("<link media='only screen and (max-device-width: 1024px)' rel='stylesheet' type='text/css' href='${tabletCSS}'/>");
                        document.write("<link rel='stylesheet' type='text/css' href='${tabletCSS}'/>");
                    }
                </script>
            </@markup>

        <#else>
            <script type="text/javascript">
                window.location.href = "/share";
            </script>
        </#if>
    </head>

    <body id="Share" class="yui-skin-${theme} alfresco-share ${type!} claro">
        <div class="sticky-wrapper">
            <div id="doc3"></div>
            <div class="sticky-push"></div>
        </div>
        <div id="card-details-footer" class="sticky-footer"></div>

        <#if hasReadPermissions>

            <#assign webframeworkConfig = config.scoped["WebFramework"]["web-framework"]!>
            <#if webframeworkConfig??>
                <#if webframeworkConfig.dojoEnabled>
                    <@markup id="setDojoConfig">
                    <script type="text/javascript">//<![CDATA[
                        var appContext = "${url.context?js_string}";

                        var dojoConfig = {
                            baseUrl: "${url.context?js_string}${webframeworkConfig.dojoBaseUrl}",
                            tlmSiblingOfDojo: false,
                            locale: (navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage)).toLowerCase(),
                            async: true,
                            parseOnLoad: false,
                            cacheBust: "${citeckUtils.getModulePackage("ecos-base-core-share").getVersion().toString()}",
                            packages: [
                                <#assign packages = webframeworkConfig.dojoPackages>
                                <#list packages?keys as name>
                                    { name: "${name}", location: "${packages[name]}"<#if webframeworkConfig.dojoPackagesMain[name]??>, main: "${webframeworkConfig.dojoPackagesMain[name]}"</#if>}<#if name_has_next>,</#if>
                                </#list>
                            ]
                        };
                        //]]></script>
                    </@>
                    <script type="text/javascript" src="${url.context}${webframeworkConfig.dojoBootstrapFile}"></script>
                </#if>
            </#if>


            <@relocateJavaScript/>

            <#assign pageArgsMap = ((page.url.templateArgs!{}) + (page.url.args!{}) + {
                "pageid": "card-details",
                "theme": "${theme!}"
            }) />

            <script type="text/javascript">//<![CDATA[
                require(['js/citeck/modules/page/card-details/card-details',
                         'js/citeck/modules/footer/share-footer',
                         'react-dom', 'react',
                         <#list pageDependencies.js as jsDep>'${jsDep}'<#if jsDep_has_next>,</#if></#list>],

                    function(components, ShareFooter, ReactDOM, React) {

                        ReactDOM.render(
                            React.createElement(components.CardDetails, {
                                alfescoUrl: window.location.protocol + "//" + window.location.host + "${url.context?js_string}/proxy/alfresco/",
                                pageArgs: {
                                <#list pageArgsMap?keys as argKey>
                                    "${argKey}":"${pageArgsMap[argKey]!}"<#if argKey_has_next>,</#if>
                                </#list>
                                }
                            }),
                            document.getElementById('doc3')
                        );
                        ReactDOM.render(
                            React.createElement(ShareFooter.default, {
                                theme: "${theme!}"
                            }),
                            document.getElementById('card-details-footer')
                        );
                });//DEBUG: ${(DEBUG?string)!"null"} CONFIG: ${common.globalConfig("client-debug", "false")}
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