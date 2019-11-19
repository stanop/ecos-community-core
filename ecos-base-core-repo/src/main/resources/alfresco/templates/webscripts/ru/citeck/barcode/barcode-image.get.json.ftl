<#escape x as jsonUtils.encodeJSONString(x)>
{"image" : <#if image??>"${image}"<#else>null</#if>}
</#escape>