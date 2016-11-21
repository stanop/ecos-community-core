<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/card/card-details.css" />

   <@script type="text/javascript" src="${url.context}/res/citeck/modules/node-denied/node-denied.js"></@script>
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></@script>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/folder-details/folder-details-panel.css" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/document-details/document-details-panel.css" />
   <@templateHtmlEditorAssets />
</@>

<#macro renderRegions regions>
	<#list regions as regionObj>
		<div class="cardlet"
			data-available-in-mobile="${regionObj.availableInMobile?string}" 
			data-position-index-in-mobile="${regionObj.positionIndexInMobile?c}"
		><@region id="${regionObj.regionId}" scope="page"/></div>
	</#list>
</#macro>

<@templateBody>
	<div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
	</div>
	<div id="bd">
		<@region id="actions-common" scope="template" />
	
		<@renderRegions topRegions />
		<div class="yui-gc">
			<div class="yui-u first">
				<@renderRegions leftRegions />
			</div>
			<div class="yui-u">
				<@renderRegions rightRegions />
			</div>
		</div>
		<@renderRegions bottomRegions />
	</div>
	
	<@region id="html-upload" scope="template"/>
	<@region id="flash-upload" scope="template"/>
	<@region id="file-upload" scope="template"/>
	<@region id="dnd-upload" scope="template"/>
	<@region id="archive-and-download" scope="template"/>
	<@region id="doclib-custom" scope="template"/>
</@>

<@templateFooter>
	<div id="alf-ft">
		<@region id="footer" scope="global" />
	</div>
</@>