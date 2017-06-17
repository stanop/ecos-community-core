<#include "/org/alfresco/components/component.head.inc">
<#assign controlId = fieldHtmlId + "-cntrl">
<input type="hidden" id="${fieldHtmlId}" name="${field.name}" value="${field.value}" />

<div class="form-field">
	<div <#if form.mode == "view" || field.disabled >style="display:none;"</#if>>
		<select id="${controlId}-workflow-selector" name="-" /><br/><br/>
		<input id="${controlId}-workflow-start" type="button" value='${msg("related_workflows.select_process_to_start")}' name="-" />
	</div>
    <br/>
    <div>${msg("related_workflows.current_task")}</div>
	<div id="${controlId}-dataTableContainer"></div>
    <br/>
    <div>${msg("related_workflows.history")}</div>
    <div id="${controlId}-dataTableHistoryContainer"></div>
</div>

<script type="text/javascript" src="${page.url.context}/res/citeck/components/related-workflows/related-workflows.js"></script>
<link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/related-workflows/related-workflows.css" />

<script type="text/javascript">
<#include "related_workflows_control.js.ftl">
</script>
