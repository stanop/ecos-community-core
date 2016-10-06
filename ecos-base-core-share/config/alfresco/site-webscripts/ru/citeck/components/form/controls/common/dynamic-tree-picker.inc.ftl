<#macro renderDynamicTreePickerJS field pickerVar="picker" valueField="nodeRef" pickerClass="Citeck.widget.DynamicTreePickerControl">

var ${pickerVar} = new ${pickerClass}("${controlId}", "${fieldHtmlId}", "${args.htmlid?js_string}").setOptions({
	<#if field.mandatory??>
		mandatory: ${field.mandatory?string},
	<#elseif field.endpointMandatory??>
		mandatory: ${field.endpointMandatory?string},
	</#if>
	<#if field.endpointMany??>
		multipleSelectMode: ${field.endpointMany?string},
	<#else>
		multipleSelectMode: false,
	</#if>
	fieldValueDelim: "${(field.control.params.fieldValueDelim!",")?js_string}",
	searchLabel: "${field.control.params.searchLabel!}",
}).setOptions({
	field: "${field.name?js_string}",
	forms: {
		nodeId: "${valueField}",
		fieldMapping: [
			<#-- default field mapping -->
			{
				formField: "${field.name}",
				itemField: "${valueField}",
			}
			<#-- additional field mappings -->
			<#if field.control.params.fieldMapping??>
			<#list field.control.params.fieldMapping?split(",") as fieldMap>
			<#assign fields = fieldMap?split(":") />
			, {
				formField: "${fields[0]?js_string}",
				itemField: "${fields[1]?js_string}",
			}
			</#list>
			</#if>
		]
	},
	<#if field.control.params.selectButtonLabel??>
	selectButtonLabel: "${field.control.params.selectButtonLabel?js_string}",
	</#if>
}).setMessages(
	${messages}
);

<#-- special treating for workflow package items controls -->
<#if field.name == "assoc_packageItems">
	<@setPackageItemOptions pickerVar />
</#if>

</#macro>

<#macro setPackageItemOptions pickerVar>

	<#local allowAddAction = false>
	<#local allowRemoveAllAction = false>
	<#local allowRemoveAction = false>

	<#if form.data['prop_bpm_packageActionGroup']?? && form.data['prop_bpm_packageActionGroup']?is_string && form.data['prop_bpm_packageActionGroup']?length &gt; 0>
		<#local allowAddAction = true>
	</#if>

	<#if form.data['prop_bpm_packageItemActionGroup']?? && form.data['prop_bpm_packageItemActionGroup']?is_string && form.data['prop_bpm_packageItemActionGroup']?length &gt; 0>
		<#local packageItemActionGroup = form.data['prop_bpm_packageItemActionGroup']>
		<#if packageItemActionGroup == "remove_package_item_actions" || packageItemActionGroup == "start_package_item_actions" || packageItemActionGroup == "edit_and_remove_package_item_actions">
			<#local allowRemoveAllAction = true>
			<#local allowRemoveAction = true>
		</#if>
	</#if>

${pickerVar}.setOptions({
	allowRemoveAction: ${allowRemoveAction?string},
	allowRemoveAllAction: ${allowRemoveAllAction?string},
	allowSelectAction: ${allowAddAction?string},
});

</#macro>

<#macro renderDynamicTreePickerHTML controlId>
	<#assign pickerId = controlId + "-picker">
	<div id="${pickerId}" class="picker yui-panel">
		<div id="${pickerId}-head" class="hd">${msg("form.control.object-picker.header")}</div>

		<div id="${pickerId}-body" class="bd">
			<div class="picker-header">
				<div id="${pickerId}-folderUpContainer" class="folder-up"><button id="${pickerId}-folderUp"></button></div>
				<div id="${pickerId}-navigatorContainer" class="navigator">
					<button id="${pickerId}-navigator"></button>
					<div id="${pickerId}-navigatorMenu" class="yuimenu">
						<div class="bd">
							<ul id="${pickerId}-navigatorItems" class="navigator-items-list">
								<li>&nbsp;</li>
							</ul>
						</div>
					</div>
				</div>
				<div id="${pickerId}-searchContainer" class="search">
					<input type="text" class="search-input" name="-" id="${pickerId}-searchText" value="" maxlength="256" />
					<span class="search-button"><button id="${pickerId}-searchButton">${msg("form.control.object-picker.search")}</button></span>
				</div>
			</div>
			<div class="yui-g">
				<div id="${pickerId}-left" class="yui-u first panel-left">
					<div id="${pickerId}-results" class="picker-items">
						<#nested>
					</div>
					<div id="${pickerId}-emptyMessage" class="no-items-message hidden">
						${msg("form.control.object-picker.no-items")}
					</div>
				</div>
				<div id="${pickerId}-right" class="yui-u panel-right">
					<div id="${pickerId}-selectedItems" class="picker-items"></div>
				</div>
			</div>
			<div class="bdft">
				<button id="${controlId}-ok" tabindex="0">${msg("button.ok")}</button>
				<button id="${controlId}-cancel" tabindex="0">${msg("button.cancel")}</button>
			</div>
		</div>
	</div>
</#macro>

<#macro renderDynamicTreePickerControlHTML field>

<#assign controlId = fieldHtmlId + "-cntrl">
<#assign is_property = field.type == "property" />

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
						<input type="hidden" id="${controlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" />
						<input type="hidden" id="${controlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" />
					</div>
				</#if>
			</span>
		</div>
	<#else>
		<#assign flatButtonMode = (field.control.params.flatButtonMode!"false") == "true"/>
		<label for="${controlId}" <#if flatButtonMode>style="float:left;margin-top:3px;"</#if>>${field.label?html}:<#if (field.endpointMandatory!false) || (field.mandatory!false)><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
		<div id="${controlId}" class="object-finder dynamic-tree-picker">
			<div id="${controlId}-currentValueDisplay" class="current-values"></div>
			<input type="hidden" id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" value="<#if is_property || form.mode != 'create'>${fieldValue}</#if>" />
			<input type="hidden" id="${controlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" value="<#if !is_property && form.mode == 'create'>${fieldValue}</#if>" />
			<input type="hidden" id="${controlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" />

			<#if field.disabled == false>
				<div id="${controlId}-itemGroupActions" class="show-picker"></div>
				<@renderDynamicTreePickerHTML controlId />
			</#if>
		</div>
	</#if>
</div>

</#macro>