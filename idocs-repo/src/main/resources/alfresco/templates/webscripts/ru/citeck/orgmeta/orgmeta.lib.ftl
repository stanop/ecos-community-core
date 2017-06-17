<#macro renderSubTypes subTypes>
[
<#list subTypes as subType>
<@renderSubType subType /><#if subType_has_next>,</#if>
</#list>
]
</#macro>

<#macro renderSubType subType>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"title": <#if subType.properties.title??>"${subType.properties.title}"<#else>null</#if>,
	"nodeRef": "${subType.nodeRef}",
	"name": "${subType.properties.name}"
}
</#escape>
</#macro>

<#macro renderTypes types>
[
<#list types as type>
<@renderType type /><#if type_has_next>,</#if>
</#list>
]
</#macro>

<#macro renderType type>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"name": "${type.name}",
	"root": "${type.root.nodeRef}"
}
</#escape>
</#macro>
