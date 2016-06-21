<#escape x as jsonUtils.encodeJSONString(x)>
[<#list templates as template>
	{
		"nodeRef": "${template.nodeRef}",
		"title": "${template.properties["cm:name"]}",
		"type": "${template.properties["dms:templateType"]}"
	}<#if template_has_next>,</#if>
</#list>]
</#escape>