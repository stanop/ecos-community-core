<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodes": [
        <@printNodes nodes/>
    ]
}
</#escape>
