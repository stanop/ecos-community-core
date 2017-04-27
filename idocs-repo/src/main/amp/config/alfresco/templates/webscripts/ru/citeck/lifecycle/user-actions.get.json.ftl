<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list actions as action>{
    <#assign actionName = action.actionParams.actionName>
    <#assign actionNameLocaleKey = 'lifecycle.action.${actionName}'>
    <#assign actionNameLocale = msg(actionNameLocaleKey)>
    <#if actionNameLocale == actionNameLocaleKey>
        "action": "${actionName}",
    <#else>
        "action": "${actionNameLocale}",
    </#if>
    "actionType": "${action.eventType!}",
    <#--"actionRef": "${action.node}",-->
    "workflowId": "${action.actionParams.workflowId!}"<#if action.actionParams.formId??>,
    "formId": "${action.actionParams.formId}"</#if><#if action.actionParams.urlId??>,
    "urlId": "${action.actionParams.urlId}"</#if>
}<#if action_has_next>,</#if></#list>
]
</#escape>