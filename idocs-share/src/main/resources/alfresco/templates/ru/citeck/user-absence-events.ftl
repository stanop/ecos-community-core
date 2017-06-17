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
        <@region id="user-absence-events" scope="template" />
        <@region id="actions-common" scope="template" />
        <@region id="data" scope="template" />
    </div>
        <@region id="html-upload" scope="template"/>
        <@region id="flash-upload" scope="template"/>
        <@region id="file-upload" scope="template"/>
        <@region id="dnd-upload" scope="template"/>
        <@region id="archive-and-download" scope="template"/>
        <@region id="doclib-custom" scope="template"/>
    </@>
</@>

<@templateFooter>
    <@markup id="alf-ft">
    <div id="alf-ft">
        <@region id="footer" scope="global" />
    </div>
    </@>
</@>