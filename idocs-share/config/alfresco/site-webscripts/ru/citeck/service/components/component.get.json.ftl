<#macro renderComponent comp>
	<#escape x as x?js_string>
{
		<#if (args.short!) == "true">
			<#list ["guid", "scope", "region-id", "source-id", "url"] as prop>
	"${prop}": "${comp.properties[prop]!}"<#if prop_has_next>,</#if>
			</#list>
		<#else/>
			<#list comp.properties?keys as prop>
	"${prop}": "${comp.properties[prop]!}"<#if prop_has_next>,</#if>
			</#list>
		</#if>
}
	</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"components": [
	<#list components as comp>
		<@renderComponent comp /><#if comp_has_next>,</#if>
	</#list>
	]
}
</#escape>