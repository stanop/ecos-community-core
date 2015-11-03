<#macro renderComponent comp>
{
	"guid": "${comp.properties.guid!}",
	"scope": "${comp.properties.scope!}",
	"region-id": "${comp.properties["region-id"]!}",
	"source-id": "${comp.properties["source-id"]!}",
	"url": "${comp.properties.url!}
}
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"oldComponents": [
	<#list oldComponents as comp>
		<@renderComponent comp /><#if comp_has_next>,</#if>
	</#list>
	],
	"newComponent": <@renderComponent newComponent />
}
</#escape>