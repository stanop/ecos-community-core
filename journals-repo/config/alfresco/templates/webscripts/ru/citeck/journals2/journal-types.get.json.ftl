<#escape x as jsonUtils.encodeJSONString(x)>
{
    "journals": [
    <#list journalTypes as journalType>
        { "journalId": "${journalType.id}" }<#if journalType_has_next>,</#if>
    </#list>
    ]
}
</#escape>