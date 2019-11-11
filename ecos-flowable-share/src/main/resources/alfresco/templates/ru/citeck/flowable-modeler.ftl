<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale}" xml:lang="${locale}">
<head>
    <meta http-equiv="Cache-Control" content="private" >
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />

    <#assign theme = (page.url.args.theme!theme)?html />

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

    <#-- polyfills for old browsers -->
    <@script type="text/javascript" src="${page.url.context}/res/js/citeck/lib/polyfill/babel-polyfill.min.js"></@script>
    <@script type="text/javascript" src="${page.url.context}/res/js/citeck/lib/polyfill/fetch.min.js"></@script>

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

<#-- This MUST be placed before the <@outputJavaScript> directive to ensure that the Alfresco namespace
     gets setup before any of the other Alfresco JavaScript dependencies try to make use of it. -->
    <@markup id="messages">
        <@generateMessages type="text/javascript" src="${url.context}/service/messages.js" locale="${locale}"/>
    </@markup>

    <@outputJavaScript/>
    <@outputCSS/>

    <#-- Common Resources -->
    <#assign preferences="" />
    <#include "/org/alfresco/components/head/resources.get.html.ftl" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/citeck/lib/flowable-modeler.css" />

    <style type="text/css">
        <#assign aikauVers = citeckUtils.getAikauVersion() />
        @font-face {
            font-family: 'Open Sans';
            font-style: normal;
            font-weight: 400;
            src: url(/share/res/js/aikau/${aikauVers}/alfresco/core/css/opensans.woff) format('woff');
        }
        @font-face {
            font-family: 'Open Sans Bold';
            font-style: normal;
            font-weight: 600;
            src: url(/share/res/js/aikau/${aikauVers}/alfresco/core/css/opensansbold.woff) format('woff');
        }
        @font-face {
            font-family: 'Open Sans Condensed';
            font-style: normal;
            font-weight: 300;
            src: url(/share/res/js/aikau/${aikauVers}/alfresco/core/css/opensanscondensed.woff) format('woff');
        }
        .alfresco-share .alfresco-header-SearchBox .alfresco-header-SearchBox-clear {
            background-image: url(/share/res/js/aikau/${aikauVers}/alfresco/css/images/Delete.PNG);
        }
    </style>
</head>

<body id="Share" class="yui-skin-${theme} alfresco-share ${type!} claro">

<div id="page-content-root" class="page-content-root">
    <div class="sticky-wrapper">
        <div id="doc3">
            <div id="alf-hd">
                <#include "/ru/citeck/include/header.ftl" />
            </div>
            <div id="bd">
                <div id="flowable-modeler"></div>
            </div>
        </div>
        <div class="sticky-push"></div>
    </div>
    <div class="sticky-footer">
        <div id="alf-ft">
            <@region id="footer" scope="global" />
        </div>
    </div>
</div>

<@relocateJavaScript/>

<script type="text/javascript">//<![CDATA[

    require(["jquery"], function() {
        var searchUrl = Alfresco.constants.PROXY_URI + '/citeck/global-properties?name=flowable.modeler.url';
        Alfresco.util.Ajax.request({
            url: searchUrl,
            successCallback: {
                scope: this,
                fn: function(response) {
                    var serverResponse = response.serverResponse ? response.serverResponse : {};
                    if (serverResponse.status === 200 && serverResponse.responseText) {
                        var responseData = eval('(' + serverResponse.responseText + ')');
                        if (responseData.data && responseData.data['flowable.modeler.url']) {
                            var modeler = document.createElement('iframe');
                            modeler.setAttribute('src', responseData.data['flowable.modeler.url']);
                            modeler.setAttribute('id', 'flowable-modeler-iframe');
                            $('#flowable-modeler').append(modeler);

                            modeler.onload = function () {
                                var stl = '<style type="text/css">' +
                                        '#main { height: 95vh!important; }' +
                                        '.ng-scope .navbar { position: inherit; margin-bottom: 0; } ' +
                                        '.ng-scope .navbar-nav > li > a { padding: 10px 20px; } ' +
                                        '.ng-scope .wrapper.full { padding: 0; } ' +
                                        '.navbar-header .landing-logo { display: none; }' +
                                        '</style>';
                                $(this).contents().find('head').append(stl);
                            };
                        } else {
                            Alfresco.util.PopupManager.displayPrompt({
                                text: "Missing 'flowable.modeler.url' in global properties"
                            });
                        }
                    }
                }
            },
            failureCallback: {
                scope: this,
                fn: function(response) {
                    Alfresco.util.PopupManager.displayPrompt({
                        text: "Flowable modeler not found"
                    });
                }
            },
            execScripts: true
        });
    });

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

</body>
</html>
