<#escape x as jsonUtils.encodeJSONString(x)>
{
	"nodeRef": "${document.nodeRef}",
	"type": "${document.typeShort}",
	"cardMode": "${cardMode!}",
	"cardlets": [
	<#list cardlets as cardlet>
		{
			"regionId": "${cardlet.properties["cardlet:regionId"]}",
			"regionColumn": "${cardlet.properties["cardlet:regionColumn"]}",
			"regionPosition": "${cardlet.properties["cardlet:regionPosition"]}"
		}<#if cardlet_has_next>,</#if>
	</#list>
	]
}
</#escape>
