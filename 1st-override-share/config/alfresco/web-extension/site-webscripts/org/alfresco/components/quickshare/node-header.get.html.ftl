<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/quickshare/node-header.css" />
   </@>

   <@markup id="js"/>

   <@markup id="widgets"/>

   <@markup id="html">
      <@uniqueIdDiv>
         <#assign el=args.htmlid?html/>
         <div class="quickshare-node-header">

            <#-- Icon -->
            <img src="${url.context}/res/components/images/filetypes/${fileExtension}-file-48.png"
                 onerror="this.src='${url.context}/res/components/images/filetypes/generic-file-48.png'"
                 title="${displayName?html}" class="quickshare-node-header-info-thumbnail" width="48" style="padding-bottom: 37px;"/>

            <#-- Title -->
            <h1 class="quickshare-node-header-info-title thin dark">${displayName?html}</h1>

            <#-- Modified form idocs-->
            <div>
               <div>
               ${msg("label.modified-by-user-on-date", (modifierFirstName!"")?html, (modifierLastName!"")?html, "<span id='${el}-modifyDate'>${modifyDate}</span>")}
               </div>
               <div class="document-download" style="padding-top: 5px;" >
                   <a title="${msg("button.download")}" class="simple-link" href="${url.context}/proxy/alfresco-noauth/api/internal/shared/node/content/${args.shareId}?a=true" style="padding:2px 2px 2px 24px; background-repeat: no-repeat; background-image:url(${url.context}/res/components/documentlibrary/actions/document-download-16.png)">${msg("button.download")}</a>
  
				</div>
               <script type="text/javascript">
                  var dateEl = YAHOO.util.Dom.get('${el}-modifyDate');
                  dateEl.innerHTML = Alfresco.util.formatDate(Alfresco.util.fromISO8601(dateEl.innerHTML), Alfresco.util.message("date-format.default"));
               </script>
            </div>
         </div>
      </@>
   </@>
</@>