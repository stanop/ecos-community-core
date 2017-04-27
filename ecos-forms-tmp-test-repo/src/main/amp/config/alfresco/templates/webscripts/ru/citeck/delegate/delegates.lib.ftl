<#macro renderDelegate delegate>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"userName": "${delegate.user.userName}",
		"firstName": "${delegate.user.person.properties.firstName!}",
		"lastName": "${delegate.user.person.properties.lastName!}",
		"nodeRef": "${delegate.user.personNodeRef}",
		"available": ${delegate.available?string},
    	"canDelete": ${delegate.canDelete?string},
    	"delegate": ${delegate.delegate?string}
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
		"delegate": ${member.delegate?string},
		"available": ${member.available?string}
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
        "delegate": ${role.delegate?string}
    }
    </#escape>
</#macro>
