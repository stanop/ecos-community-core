<#include "../search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "metadata": {
        "query": "${query}",
        "maxItems": "${maxItems}",
        "skipCount": "${skipCount}",
        "totalCount": "${totalCount}"
    },
    "results": [
        <@printNodes result />
    ]
}
</#escape>