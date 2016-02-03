<#include "dynamic-tree-picker.inc.ftl" />

<#macro renderKeyValuePickerJS field params = {}>

<#assign controlId = fieldHtmlId + "-cntrl">

<#assign itemType = params.itemType!field.control.params.itemType!"item" />
<#assign itemKey = params.itemKey!field.control.params.itemKey!"id" />
<#assign itemTitle = params.itemTitle!field.control.params.itemTitle!"{name}" />
<#assign itemLink = params.itemTitle!field.control.params.itemLink!"" />
<#assign itemURL = params.itermURL!field.control.params.itemURL! />
<#assign itemURLresults = params.iterURLresults!field.control.params.itemURLresults! />
<#assign rootURL = params.rootURL!field.control.params.rootURL! />
<#assign rootURLresults = params.rootURLresults!field.control.params.rootURLresults! />
<#assign searchURL = params.searchURL!field.control.params.searchURL! />
<#assign searchURLresults = params.searchURLresults!field.control.params.searchURLresults! />

<script type="text/javascript">//<![CDATA[
(function() {
	<@renderDynamicTreePickerJS field "picker" itemKey />
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
				"get": "${itemURL}",
				"resultsList": "${itemURLresults}",
			},
		},
		children: {
			"root": {
				"format": "item",
				"get": "${rootURL}",
				"resultsList": "${rootURLresults}",
			},
			"search": {
				"format": "item",
				"get": "${searchURL}",
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
		<#if field.control.params.errors??>
		errors: ${field.control.params.errors},
		</#if>
	};
	picker.setOptions({
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
			link: {
				"${itemType}": { 
					url: "${itemLink?js_string}"
				}
			},
		},
	});
})();
//]]></script>

</#macro>

<#macro renderKeyValuePickerHTML field params = {}>
<@renderDynamicTreePickerControlHTML field />
</#macro>