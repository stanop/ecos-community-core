<#escape x as jsonUtils.encodeJSONString(x)>{
	"types": [
		<#list allTypes as type>
		{
			"nodeRef": "${type.nodeRef}",
			"name": "${type.name}",
			"onSite": ${siteTypes?seq_contains(type)?string}
		}<#if type_has_next>,</#if>
		</#list>
	]
}</#escape>