<@markup id="css">
    <@link rel="stylesheet" href="${page.url.context}/res/citeck/components/node-header/node-header.css" />
</@>

<#function nodeURL node={}>
    <#assign site_url = url.context + "/page">
    <#if node.isContainer>
        <#switch locationType>
            <#case "documents">
                <#assign site_url = site_url + "/site/" + site + "/documentlibrary">
                <#break>
            <#case "repository">
                <#assign site_url = site_url + "/repository">
                <#break>
            <#case "archive">
                <#assign site_url = site_url + "/console/admin-console/trashcan">
                <#break>
        </#switch>
        <#if (node.displayPath?length > 0)>
            <#assign site_url = site_url + "?path=" + node.displayPath>
        </#if>
    <#else>
        <#assign site_url = site_url + "/document-details?nodeRef=" + node.nodeRef>
    </#if>
    <#return site_url>
</#function>

<#function pathCssClass isContainer notFirstItem>
    <#assign cssClass = "folder-link">
    <#if notFirstItem>
        <#if isContainer>
            <#assign cssClass = cssClass + " folder-open">
        <#else>
            <#assign cssClass = cssClass + " document-open">
        </#if>
    </#if>
    <#return cssClass?html>
</#function>

<#macro renderDetailsPaths pathItems=[]>
    <#list pathItems as path>
        <#if path_index != 0>
            <span class="separator"> &gt; </span>
        </#if>
        <span class="${pathCssClass(path.isContainer, path_index != 0)}">
        <#if path.showLink>
            <a href="${nodeURL(path)}"><#if path_index == 0>${msg("label.${locationType}")?html}<#else>${path.name?html}</#if></a>
        <#else>
            <#if path_index == 0>${msg("label.${locationType}")?html}<#else>${path.name?html}</#if>
        </#if>
        </span>
    </#list>
</#macro>

<@markup id="html">
    <#if (path?size > 0)>
        <div class="node-header-slim">
            <div class="node-info">
                <#if showPath == "true">
                    <div class="node-path-visible">
                        <@renderDetailsPaths path />
                    </div>
                </#if>
            </div>
        </div>
    </#if>
</@>