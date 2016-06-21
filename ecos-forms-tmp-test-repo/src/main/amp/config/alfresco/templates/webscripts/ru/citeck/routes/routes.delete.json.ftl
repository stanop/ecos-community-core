<#escape x as jsonUtils.encodeJSONString(x)>
    {   
        <#if message??>
            "message": "${message?string}",
        </#if>
        "status": "${status?string}"
    }
</#escape>