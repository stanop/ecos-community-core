<#assign el = args.htmlid?html />

<#if view??>
   <div id="${el}-body" 
        class="document-details-panel node-view <#if args.class??>${args.class?string}</#if>">

      <h2 id="${el}-heading" class="thin dark">
         ${msg(args.header!"header.view")}
         <span class="alfresco-twister-actions <#if args.hideEditAction?? && args.hideEditAction == "true">hidden</#if>">
            <#if writePermission?? && writePermission>
               <a class="edit" href="${url.context}/page/node-edit?nodeRef=${viewNodeRef!args.nodeRef}<#if args.viewId??>&viewId=${args.viewId}</#if>"></a>
            </#if>
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

      <#if args.style??>
         <style type="text/css">
            ${args.style?string}
         </style>
      </#if>
   </div>
</#if>

