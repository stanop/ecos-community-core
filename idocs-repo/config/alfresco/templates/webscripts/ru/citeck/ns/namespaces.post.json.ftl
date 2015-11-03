<#escape x as jsonUtils.encodeJSONString(x)>
{
	"namespaces": {
	<#list mapping?keys as prefix>
		"${prefix}": "${mapping[prefix]}"<#if prefix_has_next>,</#if>
	</#list>
	}
}
</#escape>