<#import "/ru/citeck/search/search-macros.ftl" as search>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#if contentDataProperties?has_content >
		"content" : {
			<#if contentDataProperties.mimetype??>
				"mimetype": <@search.printValue contentDataProperties.mimetype null />,
			</#if>
			<#if contentDataProperties.size??>
				"size": <@search.printValue contentDataProperties.size null />,
			</#if>
			<#if contentDataProperties.encoding??>
				"encoding": <@search.printValue contentDataProperties.encoding null />
			</#if>
		},
	</#if>

	<#if properties?has_content >
		<#list properties as property>
			"${property.key}": <#if property.value??><@search.printValue property.value null/><#else>null</#if><#if property_has_next>,</#if>
		</#list>
	</#if>
}
</#escape>