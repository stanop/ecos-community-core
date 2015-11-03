<#include "/ru/citeck/components/form/controls/select-macro.ftl" />
<script type="text/javascript">//<![CDATA[
<#assign params = field.control.params />

(function() {
	var select = new Alfresco.SelectControl("${fieldHtmlId}").setOptions({
		optionsUrl: "/share/proxy/alfresco/api/classesWithFullQname?cf=aspect",
		mode: "${form.mode}",
		responseType:  YAHOO.util.DataSource.TYPE_JSARRAY,
		titleField: "prefixedName",
		valueField: "name",
		sortKey: "prefixedName",
		<#if field.value??>originalValue: "${field.value?js_string}",</#if>
		<#if params.selectedItem??>selectedItem: "${params.selectedItem}",</#if>
		<#if params.responseSchema??>responseSchema: ${params.responseSchema},</#if>
		<#if params.requestParam??>requestParam: "${params.requestParam}",</#if>
		<#if params.resultsList??>resultsList: "${params.resultsList}",</#if>
	}).setMessages(${messages});
})();
//]]></script>

<@selectFieldHTML "${fieldHtmlId}" field/>
