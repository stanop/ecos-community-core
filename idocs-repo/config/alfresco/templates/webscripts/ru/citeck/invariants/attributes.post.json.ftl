<#escape x as jsonUtils.encodeJSONString(x)>
{
	"attributes": [
	<#list attributes as attribute>
		{
			"name": "${attribute.name}",
			"type": "${attribute.type}",
			"datatype": "${attribute.datatype}",
			"nodetype": <#if attribute.nodetype??>"${attribute.nodetype}"<#else>null</#if>,
			"javaclass": "${attribute.javaclass}"
		}<#if attribute_has_next>,</#if>
	</#list>
	]
}
</#escape>