<#import "/ru/citeck/search/search-macros.ftl" as search>
<#escape x as jsonUtils.encodeJSONString(x)>
{
"metadata": {
"nodeRef": "${args.nodeRef}"
},
"roles": [
    <@search.printNodes roles />
]
}
</#escape>