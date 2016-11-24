<#escape x as jsonUtils.encodeJSONString(x)>
{"defaultValue" : <#if defaultValue??>"${defaultValue}"<#else>null</#if>}
</#escape>