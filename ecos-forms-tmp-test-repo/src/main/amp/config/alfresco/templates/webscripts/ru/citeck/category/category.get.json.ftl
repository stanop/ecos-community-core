<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data": {
        <#if data.name??>
            "name": "${data.name}"
        </#if>
    }
}
</#escape>