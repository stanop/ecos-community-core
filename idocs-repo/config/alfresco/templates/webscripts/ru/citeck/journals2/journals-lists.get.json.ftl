<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"journalsLists": [
	<#list journalsLists as journalsList>
		{
			"id": "${journalsList.name}",
			"title": "${journalsList.title!}"
		}<#if journalsList_has_next>,</#if>
	</#list>
	]
}
</#escape>
    
