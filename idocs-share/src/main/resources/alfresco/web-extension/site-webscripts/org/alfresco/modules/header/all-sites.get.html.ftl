<#assign siteActive = args.siteId?? && (siteTitle?length > 0)>
<#assign id = args.htmlid?html>
<#assign id_js = id?js_string>
<script type="text/javascript">//<![CDATA[
   Alfresco.util.ComponentManager.get("${id_js}").setMessages(${messages});
//]]></script>
<div id="${id}-sites-menu" class="yuimenu menu-with-icons">
   <div class="bd">
      <#assign sitesDisplay><#if userSites?size != 0>block<#else>none</#if></#assign>
      <ul id="${id}-sites" class="site-list separator" style="display: ${sitesDisplay}">
      <#if userSites?size != 0>
         <#list userSites as site>
         <li>
            <a href="${url.context}/page?site=${site.shortName}">${site.title?html}</a>
         </li>
         </#list>
      </#if>
      </ul>
	  <#if user.isAdmin>
      <ul class="site-finder-menuitem<#if !user.isGuest> separator</#if>">
         <li>
            <a href="${url.context}/page/custom-site-finder">${msg("label.find-sites")}</a>
         </li>
      </ul>
      <ul class="create-site-menuitem">
         <li>
            <a href="#" onclick='Alfresco.module.getCreateSiteInstance().show(); return false;'>${msg("label.create-site")}</a>
         </li>
      </ul>
	  </#if>
   </div>
</div>