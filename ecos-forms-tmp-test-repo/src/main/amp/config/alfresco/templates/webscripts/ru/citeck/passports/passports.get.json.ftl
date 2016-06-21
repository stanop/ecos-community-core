<#escape x as jsonUtils.encodeJSONString(x)>
{
	"userName": "${args.userName}",
	"passports": [
	<#list passports as passport>
		{
			"nodeRef": "${passport.node.nodeRef}"
			, "date": "${xmldate(passport.created)}"
			, "canRead": ${passport.node.hasPermission("Read")?string}
			, "canWrite": ${passport.node.hasPermission("Write")?string}
		<#if passport.node.hasPermission("Read")>
			<#assign passportProperties = ["pass:series", "pass:number", "pass:issuingAuthority", "pass:issueDate", "pass:subdivisionCode", "pass:info"] />
			, "info": "${passport.node.properties['tk:kind'].name}<#list passportProperties as prop><#if 
				passport.node.properties[prop]??><#assign value = passport.node.properties[prop] /><#assign title = nodeService.getPropertyTitle(prop) /><#if 
				value?is_date>; ${title}: ${value?string("dd.MM.yyyy")}<#elseif 
				value != "">; ${title}: ${value}</#if></#if></#list>"
		</#if>
		}<#if passport_has_next>,</#if>
	</#list>
	]
}
</#escape>