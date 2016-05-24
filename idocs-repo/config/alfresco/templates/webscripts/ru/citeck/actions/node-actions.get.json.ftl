<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list actions as action>{
    "title": "${action.title}",
    "url": "${action.url}",
    "docNodeRef": "${action.node}",
    "actionType": "${action.actionType}",
    "context": "${action.context}"
}<#if action_has_next>,</#if></#list>
]
</#escape>