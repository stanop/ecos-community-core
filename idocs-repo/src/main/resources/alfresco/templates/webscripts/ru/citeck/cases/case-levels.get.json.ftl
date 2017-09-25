<#escape x as jsonUtils.encodeJSONString(x)>
{
	"nodeRef": "${caseNode.nodeRef}",
	"levels": [
	<#list levels as level>
	{
		"nodeRef": "${level.node.nodeRef}",
		"name": "${level.node.name}",
		"title": "${level.node.properties.title!}",
		"description": "${level.node.properties.description!}",
		"current": ${level.current?string},
		"completed": ${level.completed?string},
		"requirements": [
		<#list level.requirements as req>
		{
			"nodeRef": "${req.node.nodeRef}",
			"name": "${req.node.name}",
			"title": "${req.node.properties.title!}",
			"description": "${req.node.properties.description!}",
			"passed": ${req.passed?string},
			"matches": [
			<#list req.matches as match>
			{
				"nodeRef": "${match.nodeRef}",
				"name": "${match.name}"
			}<#if match_has_next>,</#if>
			</#list>
			]
		}<#if req_has_next>,</#if>
		</#list>
		]
	}<#if level_has_next>,</#if>
	</#list>
	]
}
</#escape>