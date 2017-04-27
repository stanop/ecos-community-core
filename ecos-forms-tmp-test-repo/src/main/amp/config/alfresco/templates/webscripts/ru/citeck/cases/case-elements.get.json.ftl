<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "metadata": {
        "caseNodeRef": "${args.nodeRef}",
        "elementType": "${args.elementType}"
    },
    "elements": [
        <@printNodes elements />
    ]
}
</#escape>