<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/citeck/components/child-forms/child-forms.js"></@script>
	<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/button-panel.js"></@script>
	<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/button-commands.js"></@script>
</@>

<@markup id="css">
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/child-forms/child-forms.css" />
</@>

<#assign id=args.htmlid?js_string />

<div id="${id}-panel" class="child-forms document-details-panel">
	<h2 id="${id}-heading" class="thin dark">
		${msg("${args.header!}")}
		<span id="${id}-heading-placeholder"></span>
		<span id="${id}-heading-actions" class="alfresco-twister-actions" style="position:relative;float:right;"></span>
	</h2>
	<div class="panel-body">
		<div id="${id}-elements" class="document-view">
		</div>
	</div>
	<script type="text/javascript">//<![CDATA[
	<#if (args.collapseHeader!'false') == 'true'>
		<#-- params is defined in config-params-to-ftl.inc.js -->
		(new Citeck.widget.ButtonPanel("${id}-heading-actions")).setOptions({
			args: {
				<#list params?keys as key>
					<#if params[key]??>"${key}": "${params[key]?js_string}"<#if key_has_next>,</#if></#if>
				</#list>
				<#if params?size gt 0 >,</#if>
				<#if ((destinationChildrenCount!0) == 0) >
					"buttonsInHeader" : "onPanelButtonCreate"
				<#elseif ((destinationChildrenCount!0) == 1) && (destinationChildrenNodeRefs?size gt 0) >
					"buttonsInHeader" : "onPanelButtonEdit",
					"nodeRef" : "${destinationChildrenNodeRefs[0]}"
				<#else>
					"destinationChildrenCount" : "${destinationChildrenCount}",
					"destinationChildrenNodeRefs_size" : "${destinationChildrenNodeRefs?size}"
				</#if>
			}
		}).setMessages(${messages});
	<#elseif (args.showCreateButton!'false') == 'true'>
		(new Citeck.widget.ButtonPanel("${id}-heading-actions")).setOptions({
			args: {
				<#list params?keys as key>
					<#if params[key]??>"${key}": "${params[key]?js_string}"<#if key_has_next>,</#if></#if>
				</#list>
				<#if params?size gt 0 >,</#if>
					"buttonsInHeader" : "onPanelButtonCreate"
			}
		}).setMessages(${messages});
	</#if>
	<#escape x as jsonUtils.encodeJSONString(x)>
		new Citeck.widget.ChildForms("${id}").setOptions({
			nodeRef: "${args.nodeRef}",
			elementsUrl: "${args.elementsUrl}",
			elementsPath: "${args.elementsPath!}",
			elementHeader: "${args.elementHeader}",
			viewFormUrl: "${args.viewFormUrl!}",
			editFormUrl: "${args.editFormUrl!}",
			createFormUrl: "${args.createFormUrl!}",
			collapseHeader: ${((args.collapseHeader!'false') == 'true')?string},
			showCreateButton: ${((args.showCreateButton!'false') == 'true')?string},
			noElementsMsg: "${msg(args.noElementsMsg!"message.empty")}"
		}).setMessages(${messages});
		Alfresco.util.createTwister("${id}-heading", "${args.twisterKey!'child-forms'}");
	</#escape>
	//]]></script>
</div>
