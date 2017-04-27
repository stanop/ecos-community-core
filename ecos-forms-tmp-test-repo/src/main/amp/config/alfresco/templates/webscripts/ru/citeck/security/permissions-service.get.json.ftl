<#escape x as jsonUtils.encodeJSONString(x)>
{
"inherit": ${data.inherit?string},
"permissions": [
    <#list data.permissions as perm>
    {
    "authority":
    {
        <#if perm.authority.avatar??>
        "avatar": "${"api/node/" + perm.authority.avatar.nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
        </#if>
    "name": "${perm.authority.name}",
    "displayName": "${perm.authority.displayName}"
    },
    "access": "${perm.access}",
    "permission": "${perm.permission}",
    "inherit": ${perm.inherit?string}
    }<#if perm_has_next>,</#if>
    </#list>
]
}
</#escape>