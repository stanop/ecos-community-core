
    var url = document.location.href;
    var cur_task_id = url.match(/taskId=([^\&]+)/)[1];

	new RWF.RelatedWorkflows( "${fieldHtmlId}" ).setOptions({
		definitionsFilter: '${(field.control.params.definitionsFilter!"(jbpm$rwf:)")?string}',
		curTaskId: cur_task_id,
		<#if form.mode == "view" || field.disabled >
		disabled: true,
		</#if>
		relWflPropName: "${field.name}"
	}).setMessages( ${messages} );
