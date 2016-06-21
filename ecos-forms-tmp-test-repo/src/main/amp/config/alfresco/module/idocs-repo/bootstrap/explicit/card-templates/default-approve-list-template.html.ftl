<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<#escape x as x?html>
<html>
<head>
<style>
table, tr, td
{
	border: 1px solid;
	border-collapse: collapse;
}
</style>
</head>
<body>
<#assign none = "(Нет)" />
<#assign dateFormat = "dd.MM.yyyy" />
<#assign dateTimeFormat = "dd.MM.yyyy HH:mm" />
<#assign signaturePlace = "______________________" />
<#assign tableWidth = "700px" />
<#assign columnWidth = "50%" />

<h1>Лист согласования</h1>
<hr />

<div>
<span><b>Документ</b></span>
<span>${document.properties["cm:name"]}</span>
</div>

<p>&nbsp;</p>

<#-- get list of all decisions -->
<#assign allDecisions = [] />
<#list nodeService.getParentAssocs(document, "bpm:packageContains")![] as parent>
    <#assign decisions = parent.childAssocs["wfcf:confirmDecisions"]![] />
    <#list decisions as decision>
        <#assign task = workflow.getTaskById(decision.properties["wfcf:confirmTaskId"]) />
        <#assign outcomeProperty = task.properties["bpm:activitiOutcomeProperty"]!"bpm:outcome" />
        <#assign outcome = task.properties[outcomeProperty] />
        <#assign role = people.getGroup (decision.properties["wfcf:confirmerRole"]) />
        <#assign user = people.getPerson(task.properties["cm:owner"]) />
        
        <#assign allDecisions = allDecisions + [ {
            "date": task.properties["bpm:completionDate"],
            "task": task,
            "outcome": outcome,
            "role": role,
            "user": user
        } ] />
    </#list>
</#list>

<#-- leave the latest decision for each role -->
<#assign decisionMap = {} />
<#list allDecisions?sort_by("date") as decision>
    <#assign decisionMap = decisionMap + { decision.role.properties.authorityName!decision.user.properties.userName : decision } />
</#list>

<table width="${tableWidth}">
	<tr>
		<td>Дата</td>
		<td>Должность</td>
		<td>Имя</td>
		<td>Решение</td>
		<td>Комментарий</td>
		<td>Подпись</td>
	</tr>

<#list decisionMap?values as decision>
    <tr>
        <td>${decision.date?string(dateFormat)}</td>
        <td>${decision.role.properties.authorityDisplayName!}</td>
        <td>${decision.user.properties.firstName!} ${decision.user.properties.lastName!}</td>
        <td>${message("workflowtask.outcome." + decision.outcome)}</td>
        <td>${decision.task.properties["bpm:comment"]!}</td>
        <td>&nbsp;</td>
    </tr>
</#list>
</table>
	
</body>
</html>
</#escape>