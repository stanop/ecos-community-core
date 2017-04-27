<#--
    Control Description:

        parameters:

            params.valueFieldName - field that will be used as value. Choose one from two available names:
                "name" or "prefixedName". By default this parameter equals to "name".

-->

<#include "/ru/citeck/components/form/controls/select-macro.ftl" />
<script type="text/javascript">//<![CDATA[
<#assign params = field.control.params />
<#assign valueField = params.valueFieldName!"name" />

(function() {
	var select = new Alfresco.SelectControl("${fieldHtmlId}").setOptions({
		optionsUrl: "/share/proxy/alfresco/api/classesWithFullQname?cf=type",
		mode: "${form.mode}",
		responseType:  YAHOO.util.DataSource.TYPE_JSARRAY,
        titleField: "prefixedName",
		valueField: "${valueField}",
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
