<#escape x as jsonUtils.encodeJSONString(x)>
{
<#list keys as key>
"${key}": "${message(key)}"<#if key_has_next>,</#if>
</#list>
}
</#escape>