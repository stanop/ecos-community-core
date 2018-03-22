<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
<!-- File-Upload -->
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/file-upload.js"></@script>
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/html-upload.js"></@script>
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/flash-upload.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions-util.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></@script>
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/document-details/document-details-panel.css" />
    <@templateHtmlEditorAssets />
</@>

<@templateBody>
   <@markup id="alf-hd">
       <div id="alf-hd">
          <@region scope="global" id="share-header" chromeless="true"/>
       </div>
   </@>
   <@markup id="bd">
       <div id="bd">
            <div class="share-form">
                <@region id="data-header" scope="page" />
                <@region id="node-view" scope="page" />
            </div>
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