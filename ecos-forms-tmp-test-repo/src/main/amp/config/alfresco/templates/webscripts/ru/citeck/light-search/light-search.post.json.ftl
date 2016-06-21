<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "query": {
        "value": "${query}",
        "language": "lucene"
    },
    "paging": {
      "maxItems": "${maxItems}",
      "skipCount": "${skipCount}",
      "totalCount": "${totalCount}"
    },
    "results": [
        <@printNodes result />
    ]
}
</#escape>