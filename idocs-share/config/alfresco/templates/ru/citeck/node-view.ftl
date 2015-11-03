<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
    <!-- CSS and JavaScript files are linked in here -->
</@>

<@templateBody>
    <div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
    </div>
       
    <div id="bd" class="static">
        <@region id="node-view" scope="page" />
        <@region id="node-view-mgr" scope="page" />
    </div>

    <@region id="html-upload" scope="template"/>
    <@region id="flash-upload" scope="template"/>
    <@region id="file-upload" scope="template"/>
    <@region id="dnd-upload" scope="template"/>
    <@region id="archive-and-download" scope="template"/>
    <@region id="doclib-custom" scope="template"/>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>