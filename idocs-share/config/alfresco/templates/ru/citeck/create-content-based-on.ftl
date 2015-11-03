<#include "/org/alfresco/include/alfresco-template.ftl" />
<@templateHeader />

<@templateBody>
<#if page.url.args.destination?? >
    <@markup id="alf-hd">
    <div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
    </div>
    </@>
    <@markup id="bd">
    <div id="bd">
        <div class="share-form">
            <@region id="create-content-based-on-mgr" scope="template" />
            <@region id="create-content-based-on" scope="template" />
        </div>
    </div>
    </@>
<#else>
    <@region id="create-content-based-on-mgr" scope="template" />
</#if>
</@>

<@templateFooter>
<#if page.url.args.destination?? >
    <@markup id="alf-ft">
    <div id="alf-ft">
        <@region id="footer" scope="global" />
    </div>
    </@>
</#if>
</@>