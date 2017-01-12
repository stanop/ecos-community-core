<#escape x as jsonUtils.encodeJSONString(x)>
[<#list types as type>
	{
		"nodeRef": "${type.nodeRef}",
		"name": "${type.properties["cm:name"]}",
		"title": "<#if type.properties["cm:title"]??>${type.properties["cm:title"]}</#if>"
	}<#if type_has_next>,</#if>
</#list>]
</#escape>