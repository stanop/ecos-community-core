<#include "/ru/citeck/search/search-macros.ftl">
<#if args.dateFormat??>
	<#assign dateFormat=args.dateFormat/>
<#else>
	<#assign dateFormat="dd.MM.yyyy HH:mm"/>
</#if>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"meta": {
		"query": "${query}",
		"maxItems": "${maxItems}",
		"skipCount": "${skipCount}",
		"resultsCount": "${nodes?size}"
	},
	"nodes": [
	<#list nodes as node>
		{
			"nodeRef": "${node.nodeRef}",
			"name": "${node.name}",
			"type": "${node.typeShort}",
			<#if node.siteShortName??>"siteShortName": "${node.siteShortName}",</#if>
			"title": "${node.properties.title!}",
			"description": "${node.properties.description!}",
			"isContainer": ${node.isContainer?string},
   			"isDocument": ${node.isDocument?string}
			<#if args.properties??>
				<#list args.properties?split(",") as prop>
					<#if args.replaceColon??>
						<#assign prop2=prop?replace(":", args.replaceColon)>
					<#else>
						<#assign prop2=prop>
					</#if>
			, "${prop2}": <#if node.properties[prop]??><@printValue node.properties[prop] dateFormat/><#else>null</#if>
				</#list>
			</#if>
		}<#if node_has_next>,</#if>
	</#list>
	]
}
</#escape>