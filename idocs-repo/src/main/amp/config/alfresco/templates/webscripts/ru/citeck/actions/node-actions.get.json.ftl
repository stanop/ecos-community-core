<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list actions as action>{
    <#list action?keys as param>
        "${param}": "${action[param]}"<#if param_has_next>,</#if>
    </#list>
}<#if action_has_next>,</#if>
</#list>
]
</#escape>