<#escape x as jsonUtils.encodeJSONString(x)>
{
    "documentTasks": [
    <#list tasks as task>
        {
            "taskId": "${task.taskId}",
            "startDate": <#if task.startDate??>"${xmldate(task.startDate)}"<#else>null</#if>,
            "dueDate": <#if task.dueDate??>"${xmldate(task.dueDate)}"<#else>null</#if>,
            "taskType": "${task.taskType}",
            "sender": "${task.sender!}",
            "lastComment": "${task.lastcomment!}",
            "reassignable":"${task.reassignable!?string("true", "false")}",
            "releasable":"${task.releasable!?string("true", "false")}",
            "claimable":"${task.claimable!?string("true", "false")}",
            "outcomePropertyName": "${task.outcomeProperty}",
            "outcome": {
            <#list task.outcomes?keys as outcome>
                "${outcome}": "${task.outcomes[outcome]}"<#if outcome_has_next>,</#if>
            </#list>
            }
        }<#if task_has_next>,</#if>
    </#list>
    ]
}
</#escape>