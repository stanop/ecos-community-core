<#include "/org/alfresco/include/alfresco-template.ftl" />
<@templateHeader />

<@templateBody>
    <@markup id="alf-hd">
    <div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
        <@region id="toolbar" scope="template" />
    </div>
    </@>
    <@markup id="bd">
    <div id="bd">
        <@region id="user-node-profile" scope="template"  />
        <@region id="node-view-mgr" scope="template" />
        <@region id="html-upload" scope="template" />
        <@region id="flash-upload" scope="template" />
        <@region id="file-upload" scope="template" />
        <@region id="dnd-upload" scope="template"/>
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