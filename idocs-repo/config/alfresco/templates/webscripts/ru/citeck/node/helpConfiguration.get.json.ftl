<#escape x as jsonUtils.encodeJSONString(x)>
{
	"meta": {
		"query": "${query}",
		"maxItems": "${maxItems}",
		"skipCount": "${skipCount}",
		"resultsCount": "${nodes?size}",
		"page": "${sitePage}"
	},
	"nodes": [
	<#list nodes as node>
		{
			"page": "${node.properties["help:page"]}",
			"selector": "${node.properties["help:selector"]}",
			"infoText": "${node.properties["help:infoText"]!}",
			<#if node.properties["help:text"]??>
			"text": "${node.properties["help:text"].nodeRef+""}",
			<#else/>
			"text": null,
			</#if>
			<#if node.properties["help:video"]??>
			"video": "${node.properties["help:video"].nodeRef+""}",
			"type": "${node.properties["help:video"].mimetype !}"
			<#else/>
			"video": null
			</#if>
		}<#if node_has_next>,</#if>
	</#list>
	]
}
</#escape>