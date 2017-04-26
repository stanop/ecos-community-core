<@markup id="js">
	<@script type="text/javascript" src="${page.url.context}/res/citeck/components/start-specified-workflow/manager.js" group="start-specified-workflow" />
</@>

<#assign startWorkflowMessageId = "workflow.start." + args.workflowId />
<#assign startWorkflowMessage = msg(startWorkflowMessageId) />
<#if startWorkflowMessage == startWorkflowMessageId>
	<#assign startWorkflowMessage = msg("button.startWorkflow") />
</#if>

<#assign failureWorkflowMessageId = "workflow.failure." + args.workflowId />
<#assign failureWorkflowMessage = msg(failureWorkflowMessageId) />
<#if failureWorkflowMessage == failureWorkflowMessageId>
	<#assign failureWorkflowMessage = msg("message.failure") />
</#if>

<script type="text/javascript">//<![CDATA[
	new Citeck.component.StartWorkflowManager("${args.htmlid}").setOptions(
	{
		failureMessage: "${failureWorkflowMessage}",
		submitButtonMessageKey: "${startWorkflowMessage}",
		defaultUrl: "${page.url.context}/page/my-tasks",
		destination: "${(page.url.args.destination!"")?js_string}",
		args: {
		<#list page.url.args?keys as arg>
			"${arg?js_string}": "${page.url.args[arg]?js_string}",
		</#list>
		},
	}).setMessages(${messages});
//]]></script>
