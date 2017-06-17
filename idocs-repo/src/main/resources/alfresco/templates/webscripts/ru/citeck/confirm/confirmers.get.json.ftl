<#escape x as jsonUtils.encodeJSONString(x)>
{
	"stages": [
	<#if (precedence??) && (precedence.stages??) && (precedence.stages?is_enumerable) >
	<#list precedence.stages as stage>
		{
			"dueDate": <#if stage.dueDate??>"${stage.dueDate}"<#else>null</#if>,
			"confirmers": [
			<#list stage.confirmers as confirmer>
				{
					"nodeRef": "${confirmer.nodeRef}",
					"fullName": "${confirmer.fullName}",
					"canCancel": ${(confirmer.canCancel!false)?string}
				}<#if confirmer_has_next>,</#if>
			</#list>
			]
		}<#if stage_has_next>,</#if>
	</#list>
	</#if>
	]
}
</#escape>