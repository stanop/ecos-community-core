<#macro stringOrNull node property>
<#escape x as jsonUtils.encodeJSONString(x)>
<#if node.properties[property]??>"${node.properties[property]}"<#else/>null</#if>
</#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"nodeRef": "${document.nodeRef}",
	"type": "${document.typeShort}",
	"cardmodes": [
	<#list cardmodes as mode>
		{
			"id": "${mode.properties["cardlet:cardModeId"]}",
			"order": "${mode.properties["cardlet:cardModeOrder"]}",
			"title": <@stringOrNull mode "cm:title" />,
			"description": <@stringOrNull mode "cm:description" />
		}<#if mode_has_next>,</#if>
	</#list>
	]
}
</#escape>