<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/document-versions-minimalistic/document-versions.css" group="document-details"/>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/document-details/revert-version.css" group="document-details"/>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/modules/document-details/historic-properties-viewer.css" group="document-details"/>
   </@>
   
   <@markup id="js">
      <#-- JavaScript Dependencies -->
      <@script type="text/javascript" src="${url.context}/res/citeck/components/document-versions-minimalistic/document-versions.js" group="document-details"/>
      <@script type="text/javascript" src="${url.context}/res/modules/document-details/revert-version.js" group="document-details"/>
      <@script type="text/javascript" src="${url.context}/res/modules/document-details/historic-properties-viewer.js" group="document-details"/>
   </@>
   
   <@markup id="widgets">
      <#if allowNewVersionUpload??>
         <@createWidgets group="document-details"/>
      </#if>
   </@>
   
   <@markup id="html">
      <@uniqueIdDiv>
         <#assign el=args.htmlid?html>
         <div id="${el}-body" class="document-versions-minimalistic document-details-panel">
            <div class="panel-body">
               <div id="${el}-currentVersion" class="version-list current-version"></div>
               <div id="${el}-olderVersions" class="version-list"></div>
            </div>
         </div>
      </@>
   </@>
</@>