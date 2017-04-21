<#import "/ru/citeck/search/search-macros.ftl" as search />
<#escape x as jsonUtils.encodeJSONString(x)>
<#assign excludeAttributes=[
    "grant:inherits",
    "grant:owner",
    "event:case",
    "event:case_added",
    "event:document",
    "event:document_added",
    "attr:parent",
    "event:initiator_added",
    "attr:parentassoc",
    "attr:parentassoc",
    "attr:noderef"
]/>
{
    "history": [
        <@search.printNodes events excludeAttributes />
    ]
}
</#escape>
