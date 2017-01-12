<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#if contentDataProperties?has_content >
		"content" : {
			<#if contentDataProperties.mimetype??>
				"mimetype": <@printValue contentDataProperties.mimetype null />,
			</#if>
			<#if contentDataProperties.size??>
				"size": <@printValue contentDataProperties.size null />,
			</#if>
			<#if contentDataProperties.encoding??>
				"encoding": <@printValue contentDataProperties.encoding null />
			</#if>
		},
	</#if>

	<#if properties?has_content >
		<#list properties as property>
			"${property.key}": <#if property.value??><@printValue property.value null/><#else>null</#if><#if property_has_next>,</#if>
		</#list>
	</#if>
}
</#escape>