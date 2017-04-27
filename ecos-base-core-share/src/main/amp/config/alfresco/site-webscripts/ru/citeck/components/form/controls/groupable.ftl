<#include "/org/alfresco/components/form/controls/common/picker.inc.ftl" />

<#assign targetField = args.htmlid?js_string + "_" + field.control.params.field />

<#assign readOnly = form.mode == "view" || field.disabled == true />
<#if readOnly>
	<!-- read-only -->
	<#assign dragItems = "false" />
	<#assign dragGroups = "false" />
<#else>
	<!-- not read-only -->
	<#assign dragItems = field.control.params.dragItems!"true" />
	<#assign dragGroups = field.control.params.dragGroups!"true" />
</#if>

<script type="text/javascript">//<![CDATA[
(function()
{
	new Citeck.widget.Groupable("${fieldHtmlId}").setOptions({
		itemsFieldId: "${targetField}",
		itemsContainerId: "${targetField + field.control.params.suffix!"-cntrl-currentValueDisplay"}",
		itemSelector: "${field.control.params.itemSelector!"> *"}",
		groupTitleSuffix: "${field.control.params.groupTitleSuffix!msg('confirm-stage.title-suffix')}",
		dragItems: ${dragItems},
		dragGroups: ${dragGroups},
		disabled: ${field.disabled?string},
	}).setMessages(
		${messages}
	);
})();
//]]></script>

<#-- for view mode we render target hidden field, because value is read from it -->
<#-- for other modes it is rendered on the field control side -->
<#if form.mode == "view">
<input type="hidden" id="${targetField}" value="${form.fields[field.control.params.field].value}" />
</#if>
<input type="hidden" id="${fieldHtmlId}" name="${field.name}" value="${field.value?html}" />
