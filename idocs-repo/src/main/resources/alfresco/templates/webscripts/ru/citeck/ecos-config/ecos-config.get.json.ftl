<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if (data.container)??>
    "container": "${data.container.nodeRef?string}",
    "value":"${data.value}"
    </#if>
}
</#escape>