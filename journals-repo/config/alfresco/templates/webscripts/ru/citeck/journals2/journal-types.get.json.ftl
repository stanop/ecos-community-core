<#escape x as jsonUtils.encodeJSONString(x)>
{
    "journalTypes": [
    <#list journalTypes as journalType>
        "${journalType.id}"<#if journalType_has_next>,</#if>
    </#list>
    ]
}
</#escape>