<#include "/ru/citeck/components/form/controls/common/dynamic-tree-picker.inc.ftl" />

<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/dynamic-tree.css" group="dynamic-tree-picker"  />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="dynamic-tree-picker" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="dynamic-tree-picker" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/dynamic-tree.js" group="dynamic-tree-picker" />
</@>

<#assign id = args.htmlid />
<#assign itemType = args.itemType!"item" />
<#assign itemKey = args.itemKey!"id" />
<#assign itemTitle = args.itemTitle!"{name}" />
<#assign itemURL = args.itemURL! />
<#assign itemURLresults = args.itemURLresults! />
<#assign rootURL = args.rootURL! />
<#assign rootURLresults = args.rootURLresults! />
<#assign searchURL = args.searchURL! />
<#assign searchURLresults = args.searchURLresults! />
<#assign autoShow = args.autoShow!"true" />

<script type="text/javascript">//<![CDATA[
	var picker = new Citeck.widget.DynamicTreePicker("${id}-picker");
	var model = {
		formats: {
			"item": {
				name: "{${itemKey}}",
				keys: [ "selected-{selected}", "${itemType}" ]
			},
			"selected-items": {
				name: "selected-items",
				keys: [ "selected-items" ],
			},
		},
		item: {
			"": {
				"format": "item",
				"get": <#if (itemURL??) && itemURL != "">${itemURL}<#else>""</#if>,
				"resultsList": "${itemURLresults}",
			},
		},
		children: {
			"root": {
				"format": "item",
				"get": <#if (rootURL??) && rootURL != "">${rootURL}<#else>""</#if>,
				"resultsList": "${rootURLresults}",
			},
			"search": {
				"format": "item",
				"get": <#if (searchURL??) && searchURL != "">${searchURL}<#else>""</#if>,
				"resultsList": "${searchURLresults}",
			},
			"selected-items": {
				"format": "item",
			},
		},
		titles: {
			"root": "{title}",
			"${itemType}": "${itemTitle?js_string}"
		},
	};
	picker.setOptions({
		<#if args.preloadSearchQuery??>
			preloadSearchQuery: "${args.preloadSearchQuery}",
		</#if>
		<#if args.preloadSearchQueryEveryTime??>
			preloadSearchQueryEveryTime: ${args.preloadSearchQueryEveryTime},
		</#if>

		model: model,
		tree: {
			buttons: {
				"${itemType}": [ "itemSelect" ],
				"selected-yes": [ "itemUnselect" ],
			},
		},
		list: {
			buttons: {
				"selected-yes": [ "itemUnselect" ],
			},
		},
		autoShow: ${autoShow}

	});
//]]></script>

<@renderDynamicTreePickerHTML id />
