<#include "/ru/citeck/components/form/controls/select-macro.ftl" />

<script type="text/javascript">//<![CDATA[
<#assign params = field.control.params />

(function() {
	var select = new Alfresco.SelectControl("${fieldHtmlId}").setOptions({
		optionsUrl: "${params.optionsUrl}",
		mode: "${form.mode}",
		<#if field.value??>originalValue: "${field.value?js_string}",</#if>
		<#if params.selectedItem??>selectedItem: "${params.selectedItem}",</#if>
		<#if params.responseType??>responseType: ${params.responseType},</#if>
		<#if params.responseSchema??>responseSchema: ${params.responseSchema},</#if>
		<#if params.requestParam??>requestParam: "${params.requestParam}",</#if>
		<#if params.titleField??>titleField: "${params.titleField}",</#if>
		<#if params.valueField??>valueField: "${params.valueField}",</#if>
		<#if params.sortKey??>sortKey: "${params.sortKey}",</#if>
		<#if params.resultsList??>resultsList: "${params.resultsList}",</#if>
	}).setMessages(${messages});
})();
//]]></script>

<@selectFieldHTML "${fieldHtmlId}" field/>
