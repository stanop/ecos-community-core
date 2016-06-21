<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
"metadata": {
"nodeRef": "${args.nodeRef}"
},
"tasks": [
    <@printNodes tasks />
]
}
</#escape>