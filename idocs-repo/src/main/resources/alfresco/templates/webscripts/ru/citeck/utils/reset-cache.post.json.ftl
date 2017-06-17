{<#list resetStatus?keys as key>
    "${key}":"${resetStatus[key]}"<#if key_has_next>,</#if>
</#list>}