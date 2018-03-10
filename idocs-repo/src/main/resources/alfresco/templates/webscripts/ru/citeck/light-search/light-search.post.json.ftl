<#import "/ru/citeck/search/search-macros.ftl" as search>
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
        <@search.printNodes result />
    ]
}
</#escape>