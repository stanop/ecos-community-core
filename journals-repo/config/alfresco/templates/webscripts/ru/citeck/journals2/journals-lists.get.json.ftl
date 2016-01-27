<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"journalsLists": [
	<#list journalsLists as journalsList>
		{
			"nodeRef": "${journalsList.nodeRef}",
			"id": "${journalsList.name}",
			"title": "${journalsList.properties['cm:title']!}"
		}<#if journalsList_has_next>,</#if>
	</#list>
	]
}
</#escape>
    
