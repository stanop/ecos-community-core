<#macro renderDeputy deputy>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"userName": "${deputy.user.userName}",
		"firstName": "${deputy.user.person.properties.firstName!}",
		"lastName": "${deputy.user.person.properties.lastName!}",
		"nodeRef": "${deputy.user.personNodeRef}",
		"available": ${deputy.available?string},
    	"canDelete": ${deputy.canDelete?string},
    "deputy": ${deputy.deputy?string},
    "isAssistant" : ${deputy.isAssistant?string}
	}
	</#escape>
</#macro>

<#macro renderAssistant assistant>
    <#escape x as jsonUtils.encodeJSONString(x)>
    {
    "userName": "${assistant.user.userName}",
    "firstName": "${assistant.user.person.properties.firstName!}",
    "lastName": "${assistant.user.person.properties.lastName!}",
    "nodeRef": "${assistant.user.personNodeRef}",
    "available": ${assistant.available?string},
    "canDelete": ${assistant.canDelete?string},
    "deputy": ${assistant.deputy?string},
    "isAssistant" : ${assistant.isAssistant?string}
    }
    </#escape>
</#macro>

<#macro renderMember member>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"userName": "${member.user.userName}",
		"firstName": "${member.user.person.properties.firstName!}",
		"lastName": "${member.user.person.properties.lastName!}",
		"nodeRef": "${member.user.personNodeRef}",
		"manage": ${member.manage?string},
		"deputy": ${member.deputy?string},
    "available": ${member.available?string},
    "isAssistant": ${member.isAssistant?string},
    "canDelete": ${member.canDelete?string}
	}
	</#escape>
</#macro>

<#macro renderRole role>
    <#escape x as jsonUtils.encodeJSONString(x)>
    {
        "fullName": "${role.group.fullName}",
        "displayName": "${role.group.displayName!}",
        "shortName": "${role.group.shortName!}",
        "manage": ${role.manage?string},
        "deputy": ${role.deputy?string}
    }
    </#escape>
</#macro>
