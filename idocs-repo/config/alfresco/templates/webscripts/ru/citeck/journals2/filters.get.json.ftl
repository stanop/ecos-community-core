<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "journalType": "${journalType}",
    "filters": <@journals.renderFilters filters />
}
</#escape>
