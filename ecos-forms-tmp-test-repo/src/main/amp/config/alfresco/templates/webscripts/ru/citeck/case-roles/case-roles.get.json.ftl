<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
"metadata": {
"nodeRef": "${args.nodeRef}"
},
"roles": [
    <@printNodes roles />
]
}
</#escape>