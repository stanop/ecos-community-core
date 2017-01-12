<#escape x as jsonUtils.encodeJSONString(x)>
{"data" : <#if data??>"${data}"<#else>null</#if>}
</#escape>
