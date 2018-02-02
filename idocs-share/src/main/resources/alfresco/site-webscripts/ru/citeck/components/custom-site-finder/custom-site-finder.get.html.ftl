<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/site-finder/site-finder.css" group="custom-site-finder"/>
   <@link href="${url.context}/res/modules/delete-site.css" group="custom-site-finder"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/citeck/components/site-finder/custom-site-finder.js" group="custom-site-finder"/>
   <@script src="${url.context}/res/modules/delete-site.js" group="custom-site-finder"/>
</@>

<@markup id="widgets">
   <@createWidgets group="custom-site-finder"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="site-finder">
         <div class="title"><label for="${el}-term">${msg("site-finder.heading")}</label></div>
         <div class="finder-wrapper">
            <div class="search-bar theme-bg-color-3">
               <div class="search-text"><input type="text" id="${el}-term" class="search-term" maxlength="256" /></div>
               <div class="search-button"><button id="${el}-button">${msg("site-finder.search-button")}</button></div>
            </div>
            <div id="${el}-sites" class="results"></div>
         </div>
      </div>
   </@>
</@>