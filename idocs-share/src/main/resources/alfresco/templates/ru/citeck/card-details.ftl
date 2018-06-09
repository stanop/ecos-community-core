<#import "/org/alfresco/import/alfresco-common.ftl" as common />
<#--
   CONSTANTS & HELPERS
-->
<#-- Global flags retrieved from share-config (or share-config-custom) -->
<#assign DEBUG = (common.globalConfig("client-debug", "false") = "true")>
<#assign AUTOLOGGING = (common.globalConfig("client-debug-autologging", "false") = "true")>
<#-- allow theme to be specified in url args - helps debugging themes -->
<#assign theme = (page.url.args.theme!theme)?html />
<#--
   Template "templateHeader" macro.
   Includes preloaded YUI assets and essential site-wide libraries.
-->
<#macro templateHeader doctype="strict">
 <#if doctype = "strict">
 <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
 <#else>
 <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 </#if>
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale}" xml:lang="${locale}">
<head>
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

 <#-- Common Resources -->
 <#include "/org/alfresco/components/head/resources.get.html.ftl" />

 <#-- Template Resources (nested content from < @templateHeader > call) -->
 <#nested>

 <@markup id="resources">
 <#-- Additional template resources -->
 </@markup>

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
</head>
</#macro>

<#macro templateBody type="">
<body id="Share" class="yui-skin-${theme} alfresco-share ${type} claro">
<div class="sticky-wrapper">
    <div id="doc3">
    <#-- Template-specific body markup -->
    <#nested>
    </div>
    <div class="sticky-push"></div>
</div>
</#macro>

<#--
   Template "templateFooter" macro.
   Pulls in template footer.
-->
<#macro templateFooter>
<div class="sticky-footer">
<#-- Template-specific footer markup -->
<#nested>
</div>
</body>
</html>
</#macro>


<@templateHeader>
   <meta http-equiv="Cache-Control" content="private" >

   <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/card/card-details.css" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/utils/citeck.css" />
   <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/action-renderer.js" />
   <@script type="text/javascript" src="${url.context}/res/citeck/modules/node-denied/node-denied.js" />
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" />
   <@script type="text/javascript" src="${url.context}/res/lib/underscore.js" />
   <@script type="text/javascript" src="${url.context}/res/citeck/utils/citeck.js" />
   <@script type="text/javascript" src="${url.context}/res/components/people-finder/people-finder.js" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/folder-details/folder-details-panel.css" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/document-details/document-details-panel.css" />
   <#include "/org/alfresco/components/form/form.css.ftl"/>
   <#include "/org/alfresco/components/form/form.js.ftl"/>
</@>

<@templateBody>
    <div id="card-details-root"></div>
</@>

<@templateFooter>
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

    <#assign pageArgsMap = ((page.url.templateArgs!{}) + (page.url.args!{}) + {
        "pageid": "card-details",
        "theme": "${theme!}"
    }) />

    <script type="text/javascript">//<![CDATA[
        require(['js/citeck/modules/card-details/card-details', 'react-dom', 'react'], function(comp, ReactDOM, React) {
            ReactDOM.render(
                React.createElement(comp.CardDetails, {
                    alfescoUrl: window.location.protocol + "//" + window.location.host + "${url.context?js_string}/proxy/alfresco/",
                    pageArgs: {
                        <#list pageArgsMap?keys as argKey>
                            "${argKey}":"${pageArgsMap[argKey]!}"<#if argKey_has_next>,</#if>
                        </#list>
                    }
                }),
                document.getElementById('card-details-root')
            );
        });//DEBUG: ${(DEBUG?string)!"null"} CONFIG: ${common.globalConfig("client-debug", "false")}
    //]]></script>

    <div id="alfresco-yuiloader"></div>
    <@relocateJavaScript/>

    <script type="text/javascript">//<![CDATA[
        Alfresco.util.YUILoaderHelper.loadComponents(true);
        <#-- Security - ensure user has a currently authenticated Session when viewing a user auth page e.g. when Back button is used -->
        <#if page?? && (page.authentication="user" || page.authentication="admin")>
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.URL_CONTEXT + "service/modules/authenticated?noCache=" + new Date().getTime() + "&a=${page.authentication?html}"
            });
        </#if>
    //]]></script>
</@>