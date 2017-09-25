<#include "/org/alfresco/include/alfresco-template.ftl" />
<@templateHeader />

<@templateBody>
    <@markup id="alf-hd">
    <div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
    </div>
    </@>
    <@markup id="bd">
    <div id="bd">
        <@region id="toolbar" scope="template" />
        <@region id="deputy" scope="template"  />
    </div>
    </@>
</@>

<@templateFooter>
    <@markup id="alf-ft">
    <div id="alf-ft">
        <@region id="footer" scope="global" />
    </div>
    </@>
</@>