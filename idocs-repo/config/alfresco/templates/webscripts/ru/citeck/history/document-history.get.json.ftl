<#import "../search/search-macros.ftl" as search />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "query": "${query!}",
    "history": [
    <@search.printNodes events />
    ]
}
</#escape>