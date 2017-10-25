<#include "search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "query": {
        "language": "${language}",
        "value": "${query}"
    },
    "paging": {
        "skipCount": <#if criteria.skip??>${criteria.skip?c}<#else>null</#if>,
        "maxItems": <#if criteria.limit??>${criteria.limit?c}<#else>null</#if>,
<#-- @deprecated -->
        "totalCount": <#if criteria.total??>${criteria.total?c}<#else>null</#if>,
        "totalItems": <#if totalCount??>${totalCount?c}<#else>null</#if>,
        "hasMore": ${hasMore?string("true", "false")}
    },
    "results": [
        <@printNodes nodes/>
    ]
}
</#escape>