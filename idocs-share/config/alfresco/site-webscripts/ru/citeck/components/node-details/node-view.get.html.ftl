<#assign el = args.htmlid?html />
<#if view??>
   <div id="${el}-body" class="document-details-panel node-view">

      <h2 id="${el}-heading" class="thin dark">
         ${msg(args.header!"header.view")}
         <span class="alfresco-twister-actions">
            <#-- TODO hide this if there is no permissions -->
            <a class="edit" href="${url.context}/page/node-edit?nodeRef=${args.nodeRef}"></a>
         </span>
      </h2>

      <div class="panel-body">
         <#include "/ru/citeck/components/invariants/view.get.html.ftl" />
      </div>

      <script type="text/javascript">//<![CDATA[
         YAHOO.Bubbling.on("metadataRefresh", function(layer, args) {
            var component = Alfresco.util.ComponentManager.get("${el}-form");
            if(!component) return;
            var full = true;
            component.runtime.node().impl().reset(full);
         });
         Alfresco.util.createTwister("${el}-heading", "node-view");
      //]]></script>

   </div>
</#if>
