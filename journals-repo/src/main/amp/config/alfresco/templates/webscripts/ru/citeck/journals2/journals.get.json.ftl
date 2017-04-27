<#import "journals.lib.ftl" as jlf />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"journals": [
	<#list journals as journal>
		<@jlf.renderJournal journal=journal full=false />
		<#if journal_has_next>,</#if>
	</#list>
	]
}
</#escape>
    
