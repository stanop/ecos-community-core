<#include "common/dynamic-tree-picker.inc.ftl" />
<#import "/org/alfresco/components/documentlibrary/include/documentlist.lib.ftl" as doclib />
<#assign controlId = fieldHtmlId + "-cntrl">

<#assign rootNodeRef = field.control.params.rootNodeRef!"alfresco://company/home" />

<#assign is_property = field.type == "property" />

<#assign doclibView = field.control.params.doclibView!"picker" />

<#assign hasUploadButton = field.control.params.hasUploadButton!true />

<#if hasUploadButton == true >
	<#assign pickerClass="Citeck.widget.UploadPickerControl" />
<#else>
	<#assign pickerClass="Citeck.widget.DynamicTreePickerControl" />
</#if>

<#if field.control.params.uploadDestination??>
	<#assign destination = field.control.params.uploadDestination />
</#if>

<#-- choose selectable types -->
<#if field.control.params.selectable??>
	<#-- if it is explicitly specified -->
	<#assign selectable = field.control.params.selectable?split(",") />
<#else>
	<#assign selectable = ["document-${doclibView}"] />
</#if>

<script type="text/javascript">//<![CDATA[
(function() {
	<@renderDynamicTreePickerJS field=field pickerVar="picker" pickerClass="${pickerClass}" />
	var model = {
		formats: {
			<#-- actionGroupId is folder-picker or document-picker -->
			"item": {
				name: "{nodeRef}",
				keys: [ "selected-{selected}", "item", "{actionGroupId}" ],
				calc: function(item) {
					item.nodeRefForURL = item.nodeRef.replace("://", "/");
				},
			},
			"selected-items": {
				name: "selected-items",
				keys: [ "selected-items" ],
			},
		},
		item: {
			"": {
				"format": "item",
				"get": "${url.context}/service/citeck/components/documentlibrary/data/node/{nodeRefForURL}?view=${doclibView}",
				"resultsList": "item",
			},
		},
		children: {
			"root": {
				"format": "item",
				"get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/${rootNodeRef?replace("://", "/")}?view=${doclibView}",
				"resultsList": "items",
			},
			"folder-${doclibView}": {
				"format": "item",
				"get": "${url.context}/service/citeck/components/documentlibrary/data/doclist/treenode/node/{nodeRefForURL}?view=${doclibView}",
				"resultsList": "items",
			},
			"selected-items": {
				"format": "item",
			},
		},
		titles: {
			"root": "{title}",
			"item": "{displayName}"
		},
		<#if field.control.params.errors??>
		errors: ${field.control.params.errors},
		</#if>
	};
	picker.setOptions({
		model: model,
		tree: {
			buttons: {
				<#list selectable as selectableKey>
				"${selectableKey}": [ "itemSelect" ],
				</#list>
				"selected-yes": [ "itemUnselect" ],
			},
		},
		list: {
			buttons: {
				"selected-yes": [ "itemUnselect" ],
			},
		},
		<#if field.control.params.uploadButtonLabel??>
		uploadButtonLabel: "${field.control.params.uploadButtonLabel?js_string}",
		</#if>
		<#if destination??>
		destinationNodeRef: "${destination?js_string}",
		</#if>
	});
})();
//]]></script>

<#if field.value?is_number>
	<#assign fieldValue=field.value?c>
<#else>
	<#assign fieldValue=field.value?html>
</#if>
<div class="form-field">
	<#if form.mode == "view">
		<div class="viewmode-field">
			<#if field.mandatory && !(field.value?is_number) && field.value == "">
				<span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
			</#if>
			<span class="viewmode-label">${field.label?html}:</span>
			<span class="viewmode-value">
				<#if fieldValue == "">
					${msg("form.control.novalue")}
				<#else>
					<div id="${controlId}" class="object-finder dynamic-tree-picker">
						<div id="${controlId}-currentValueDisplay" class="current-values read-only"></div>
						<input type="hidden" id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" value="${fieldValue}" />
					</div>
				</#if>
			</span>
		</div>
	<#else>
		<label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>

		<div id="${controlId}" class="object-finder dynamic-tree-picker">

		<div id="${controlId}-currentValueDisplay" class="current-values"></div>

		<input type="hidden" id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" value="${fieldValue}" />
		<input type="hidden" id="${controlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" />
		<input type="hidden" id="${controlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" />

		<#if field.disabled == false>
		<div id="${controlId}-itemGroupActions" class="show-picker"></div>
		<@renderDynamicTreePickerHTML controlId />
		</#if>

		</div>
	</#if>
</div>
