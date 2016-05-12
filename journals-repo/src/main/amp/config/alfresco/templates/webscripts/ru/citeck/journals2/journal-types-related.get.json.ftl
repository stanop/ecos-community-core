<#escape x as jsonUtils.encodeJSONString(x)>
[
        <#list mapTypes as mapType>
            {
                "journalType": "${mapType.journalTypes}",
                "type": "${mapType.type}"
            }<#if mapType_has_next>,</#if>
        </#list>
]
</#escape>