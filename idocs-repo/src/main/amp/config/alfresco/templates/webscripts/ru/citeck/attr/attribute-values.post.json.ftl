<#import "/ru/citeck/search/search-macros.ftl" as macros>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"attributes": [
	<#list values as record>
		{
			"nodeRef": "${record.nodeRef}",
			"attribute": "${record.attribute}",
			"persisted": ${record.persisted?string},
			"value": <#if !(record.value??)>
				null
			<#elseif nodeService.isContentProperty(record.value)>
				<@macros.printValue companyhome.nodeByReference[record.nodeRef].properties[record.attribute] />
			<#else>
				<@macros.printValue record.value />
			</#if>
		}<#if record_has_next>,</#if>
	</#list>
	]
}
</#escape>