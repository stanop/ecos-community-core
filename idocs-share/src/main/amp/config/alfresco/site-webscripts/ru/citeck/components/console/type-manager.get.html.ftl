<#import "/ru/citeck/views/views.lib.ftl" as views />

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@views.nodeViewStyles />

   <@link href="${url.context}/res/citeck/components/console/type-manager.css" group="console"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@views.nodeViewScripts />

   <@script src="${url.context}/res/components/console/consoletool.js" group="console"/>
   <@script src="${url.context}/res/citeck/components/console/type-manager.js" group="console"/>
</@>

<@markup id="widgets">
   <@createWidgets group="console"/>
</@>

<@markup id="html">
   <#assign el=args.htmlid?html>
   <@uniqueIdDiv>
      <div id="${el}-body" class="type-manager typeview">
         <!-- List panel -->
         <div id="${el}-list">
            <div class="yui-u first">
               <div class="title">${msg("title.type-manager")}</div>
            </div>
            <div class="yui-u align-left">
               <div id="${el}-type-manager" class="type"></div>
            </div>
         </div>
      </div>
   </@>
</@>