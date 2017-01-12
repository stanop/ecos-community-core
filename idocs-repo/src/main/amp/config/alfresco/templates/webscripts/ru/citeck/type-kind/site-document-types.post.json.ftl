<#escape x as jsonUtils.encodeJSONString(x)>{
	"added": [
		<#list added as type>
		"${type.nodeRef}"<#if type_has_next>,</#if>
		</#list>
	],
	"removed": [
		<#list removed as type>
		"${type.nodeRef}"<#if type_has_next>,</#if>
		</#list>
	]
}</#escape>