<#escape x as jsonUtils.encodeJSONString(x)>
{
	"templates": [
		<#list templates as template>
		{
			"nodeRef": "${template.nodeRef}",
			"name": "${template.name}"
		}<#if template_has_next>,</#if>
		</#list>
	]
}
</#escape>