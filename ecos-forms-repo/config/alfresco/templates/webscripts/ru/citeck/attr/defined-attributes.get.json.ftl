<#escape x as jsonUtils.encodeJSONString(x)>
{
	"attributes": [
	<#list attributes as attribute>
		"${attribute}"<#if attribute_has_next>,</#if>
	</#list>
	]
}
</#escape>