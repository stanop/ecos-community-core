<#import "/ru/citeck/search/search-macros.ftl" as search>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodes": [
        <@search.printNodes nodes/>
    ]
}
</#escape>
