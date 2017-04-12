<#escape x as jsonUtils.encodeJSONString(x)>
{
<#list attributes?keys as att>
"${att}": "${attributes[att]}"<#if att_has_next>,</#if>
</#list>
}
</#escape>