<#import "../orgmeta/orgmeta.lib.ftl" as orgmeta />

<#-- render authority (various types of groups and users) -->
<#macro renderAuthority authority>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#-- basic authority information -->
	"authorityType": "${authority.authorityType}",
	"shortName": "${authority.shortName}",
	"displayName": "${authority.displayName}",
	<#if authority.authorityType == "USER">
	"fullName": "${authority.userName}",
	"firstName": "${authority.person.properties.firstName!}",
	"lastName": "${authority.person.properties.lastName!}",
    <#assign available = authority.person.properties["delegate:available"]!true />
	"available": ${available?string},
	"nodeRef": "${authority.personNodeRef}"
	<#else>
	"fullName": "${authority.fullName}",
		<#assign branchTypeName = authority.groupNode.properties["org:branchType"]! />
		<#assign roleTypeName = authority.groupNode.properties["org:roleType"]! />
		<#if branchTypeName != "">
	"groupType": "branch",
	"groupSubType": "${branchTypeName}",
		<#elseif roleTypeName != "">
	"groupType": "role",
	"groupSubType": "${roleTypeName}",
	"roleIsManager": ${groupTypes["role"][roleTypeName].properties["org:roleIsManager"]?string},
		<#else>
	"groupType": "group",
		</#if>
	"nodeRef": "${authority.groupNodeRef}"</#if><#nested/>
}
</#escape>
</#macro>

<#macro renderAuthorities authorities>
[
<#list authorities as authority>
<@renderAuthority authority />
<#if authority_has_next>,</#if>
</#list>
]
</#macro>

