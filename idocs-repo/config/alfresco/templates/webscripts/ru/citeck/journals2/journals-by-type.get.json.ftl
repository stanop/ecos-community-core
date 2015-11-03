<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "id": "type-${args.type}",
    "title": "",
    "journals": <@journals.renderJournals journals />
}
</#escape>
    
