<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />
<#assign controlId = fieldHtmlId + "-cntrl">

<div class="form-field" >
<#if form.mode == "view">
    <div class="viewmode-field">
        <span class="viewmode-label">${field.label?html}:</span>
        <span class="viewmode-value">${field.value?html}</span>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
	<#if form.mode == "create">
		<input id="${fieldHtmlId}" type="text" name="${field.name?js_string}" value="${field.value?js_string}" />
	<#else>
		<input id="${fieldHtmlId}" type="text" name="${field.name?js_string}" value="${field.value?replace("^GROUP_", "", "r")?js_string}" disabled="true" />
	</#if>
</#if>
</div>