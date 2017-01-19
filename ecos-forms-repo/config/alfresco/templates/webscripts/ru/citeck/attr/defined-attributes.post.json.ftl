<#escape x as jsonUtils.encodeJSONString(x)>
{
<#list attributes?keys as key>
	<#assign defined = attributes[key] />
	"${key}": [
	<#list defined as attribute>
		"${attribute}"<#if attribute_has_next>,</#if>
	</#list>
	]<#if key_has_next>,</#if>
</#list>
}
</#escape>