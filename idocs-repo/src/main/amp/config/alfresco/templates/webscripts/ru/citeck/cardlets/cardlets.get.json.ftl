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
			"regionPosition": "${cardlet.properties["cardlet:regionPosition"]}",

			"availableInMobile": <#if cardlet.properties["cardlet:availableInMobile"]??>${cardlet.properties["cardlet:availableInMobile"]?string}<#else>true</#if>,
			"positionIndexInMobile": <#if cardlet.properties["cardlet:positionIndexInMobile"]??>${cardlet.properties["cardlet:positionIndexInMobile"]?c}<#else>-1</#if>,
		}<#if cardlet_has_next>,</#if>
	</#list>
	]
}
</#escape>
