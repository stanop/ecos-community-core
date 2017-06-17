<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/comments/comments-list.css" group="comments"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/comments/comments-list.js" group="comments"/>
</@>

<@markup id="widgets">
   <#if nodeRef??>
      <@createWidgets group="comments"/>
      <@inlineScript group="comments">
         YAHOO.util.Event.onContentReady("${args.htmlid?js_string}-heading", function() {
            Alfresco.util.createTwister("${args.htmlid?js_string}-heading", "Comments");
         });
      </@>
   </#if>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#if nodeRef??>
         <#assign el=args.htmlid?html>
         <div id="${el}-body" class="comments-list document-details-panel">
            <h2 id="${el}-heading" class="thin dark">${msg("header.comments")}</h2>
            <div class="panel-body">
               <div id="${el}-add-comment">
                  <div id="${el}-add-form-container" class="theme-bg-color-4 hidden"></div>
               </div>
               <div class="comments-list-actions">
                  <div class="left">
                     <div id="${el}-actions" class="hidden">
                        <button class="alfresco-button" name=".onAddCommentClick">${msg("button.addComment")}</button>
                     </div>
                  </div>
                  <div class="right">
                     <div id="${el}-paginator-top"></div>
                  </div>
                  <div class="clear"></div>
               </div>
               <hr class="hidden"/>
               <div id="${el}-comments-list"></div>
               <hr class="hidden"/>
               <div class="comments-list-actions">
                  <div class="left">
                  </div>
                  <div class="right">
                     <div id="${el}-paginator-bottom"></div>
                  </div>
                  <div class="clear"></div>
               </div>
            </div>
         </div>

         <script type="text/javascript">
            
         </script>
      </#if>
   </@>
</@>