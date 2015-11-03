<#include "/org/alfresco/components/form/controls/date.ftl" />
<#if (form.mode == "create") && ((field.control.params.appendDaysToCurrentValue??) || (field.control.params.appendHoursToCurrentValue??))>
	<#assign controlId = fieldHtmlId + "-cntrl">
<script type="text/javascript">//<![CDATA[
	var x = Alfresco.util.ComponentManager.get("${controlId}");
	var d = new Date();
	<#if field.control.params.appendDaysToCurrentValue??>
	d.setDate(d.getDate() + ${field.control.params.appendDaysToCurrentValue});
	</#if>
	<#if field.control.params.appendHoursToCurrentValue??>
	d.setHours(d.getHours() + ${field.control.params.appendHoursToCurrentValue});
	</#if>
	x.setOptions({ currentValue: Alfresco.util.toISO8601(d) });
//]]></script>
</#if>
