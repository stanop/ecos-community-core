<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dashboard/customise-layout.css" group="dashboard"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/citeck/components/dashboard/customise-layout.js" group="dashboard"/>
</@>

<@markup id="widgets">
   <@createWidgets group="dashboard"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <div class="customise-layout">
         <div id="${args.htmlid}-hideCurrentLayout-div" style="display: none;"></div>
         
         <div class="information">
            <div class="currentLayout">
               <h2>${msg("section.currentLayout")}</h2>
               <div id="${args.htmlid}-currentLayoutDescription-span" class="text">${currentLayout.description}</div>
            </div>
            <div class="availableLayouts">
               <h3>${msg("section.selectNewLayout")}</h3>
               <div class="text">${msg("label.layoutWarning")}</div>
            </div>
         </div>

         <div class="layouts">
            <ul id="${args.htmlid}-layout-ul">
               <#list layouts?values as layout>
                  <li id="${args.htmlid}-layout-li-${layout.templateId}" class="layout">
                     <img id="${args.htmlid}-select-img-${layout.templateId}" class="layoutIcon" src="${url.context}/res/citeck/components/dashboard/images/${layout.templateId}.png" alt="${layout.templateId}" width="270" />
                     <div class="layoutDescription">${layout.description}</div>
                  </li>
               </#list>
            </ul>
         </div>
      </div>
   </@>
</@>