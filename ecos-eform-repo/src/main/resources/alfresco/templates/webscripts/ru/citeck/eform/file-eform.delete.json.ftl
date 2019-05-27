<#escape x as jsonUtils.encodeJSONString(x)>
    { "result": "<#if result??>${result}<#else>null</#if>" }
</#escape>