<#macro renderGroups groups>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#list groups?keys as groupName>
	<#assign group = groups[groupName] />
	"${groupName}": <@renderGroup group /><#if groupName_has_next>,</#if>
	</#list>
}
</#escape>
</#macro>

<#macro renderGroup group>
<#escape x as jsonUtils.encodeJSONString(x)>
	<#if group.userName??>
	{
		"userName": "${group.userName}"
	}
	<#else/>
	{
		"shortName": "${group.shortName}",
		"displayName": "${group.displayName}",
		"type": "${group.type}",
		"children": <@renderGroups group.children />
	}
	</#if>
</#escape>
</#macro>

<@renderGroups groups />