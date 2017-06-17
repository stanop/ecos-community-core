<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/document-details/document-workflows.css" group="inactive-workflows" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/citeck/components/document-details/inactive-workflows.js" group="inactive-workflows" />
</@>

<#if workflows??>
   <#assign el=args.htmlid?js_string>

   <div id="${el}-body" class="document-details-panel document-inactive-workflows">
      <h2 id="${el}-heading" class="thin dark">
         <#if (args.active!"false") == "true">
            ${msg("header.workflows")}
         <#else />
            ${msg("header.inactive-workflows")}
         </#if>
      </h2>

      <div class="panel-body">
         <div class="info">
            <#if workflows?size == 0>
               <#if (args.active!"false") == "true">
                     ${msg("label.noActiveWorkflows")}
               <#else />
                     ${msg("label.noInactiveWorkflows")}
               </#if>
            <#else>
               <#if (args.active!"false") == "true">
                     ${msg("label.activeWorkflows")}
               <#else />
                     ${msg("label.inactiveWorkflows")}
               </#if>
            </#if>
         </div>

         <#if workflows?size &gt; 0>
            <hr/>
            <div class="document-workflows">
               <#list workflows as workflow>
                  <div class="workflow <#if !workflow_has_next>workflow-last</#if>">
                     <#if workflow.initiator?? && workflow.initiator.avatarUrl??>
                        <img src="${url.context}/proxy/alfresco/${workflow.initiator.avatarUrl}" alt="${msg("label.avatar")}"/>
                     <#else>
                        <img src="${url.context}/components/images/no-user-photo-64.png" alt="${msg("label.avatar")}"/>
                     </#if>

                     <div>
                        <a href="${url.context+"/page/workflow-details?workflowId=" + workflow.id?js_string + "&nodeRef=" + (args.nodeRef!"")?js_string}">
                           <#if workflow.message?? && workflow.message?length &gt; 0>
                              ${workflow.message?html}
                           <#else>
                              ${msg("workflow.no_message")?html}
                           </#if>
                        </a>
                        <div class="title">${workflow.title?html}</div>
                     </div>

                     <div class="clear"></div>
                  </div>
               </#list>
            </div>
         </#if>

      </div>

      <#if workflows?size &gt; 0>
         <script type="text/javascript">
            new Citeck.widget.InactiveWorkflows("${el}").setOptions({
               isActiveWorkflows: ${args.active},
               sortBy: "${args.sortBy}",
               nodeRef: "${nodeRef?js_string}",
               siteId: <#if site??>"${site?js_string}"<#else>null</#if>,
               destination: <#if destination??>"${destination}"<#else>null</#if>
            }).setMessages(${messages});
         </script>
      </#if>

      <script type="text/javascript">
         Alfresco.util.createTwister("${el}-heading", "InactiveDocumentWorkflows");
      </script>
   </div>
</#if>