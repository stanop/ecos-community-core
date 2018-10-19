<@standalone>

   <@markup id="css" >
      <#include "/org/alfresco/components/preview/include/web-preview-css-dependencies.lib.ftl" />
   </@>

   <@markup id="js" >
      <#include "/org/alfresco/components/preview/include/web-preview-js-dependencies.lib.ftl" />
   </@>

   <@markup id="widgets">
      <#if node??>
         <@createWidgets group="${dependencyGroup}"/>
      </#if>
   </@>

   <#assign el=args.htmlid?js_string>
   <@markup id="html">
      <@uniqueIdDiv>
         <#if node??>
            <#assign el=args.htmlid?html>

            <div id="${el}-body" class="web-preview document-details-panel">
               <h2 id="${el}-heading" class="thin dark">
                  ${msg("title")}
               </h2>
               <div id="${el}-previewer-div" class="previewer">
                  <div class="message"></div>
               </div>
            </div>
         </#if>
      </@>

      <script type="text/javascript">//<![CDATA[
         Alfresco.util.createTwister("${el}-heading", "webPreview");
         YUIDom.replaceClass("${el}-heading", "alfresco-twister-closed", "alfresco-twister-open");
         YUIDom.setStyle("${el}-previewer-div", 'display', 'block');
      //]]></script>
   </@>

</@standalone>
