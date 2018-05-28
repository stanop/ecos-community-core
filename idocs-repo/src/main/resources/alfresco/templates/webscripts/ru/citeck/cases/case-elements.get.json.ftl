<#import "/ru/citeck/search/search-macros.ftl" as search>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "metadata": {
        "caseNodeRef": "${args.nodeRef}",
        "elementType": "${args.elementType}"
    },
    "elements": [
        <@search.printNodes elements />
    ]
}
</#escape>