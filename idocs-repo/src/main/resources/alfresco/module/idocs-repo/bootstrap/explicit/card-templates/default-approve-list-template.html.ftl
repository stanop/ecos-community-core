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
<#assign clazz = "ru.citeck.ecos.history.records.model.HistoryRecordModel" />
<#assign query = "{\"sourceId\": \"history\", \"language\": \"document\", \"query\": \"{\\\"nodeRef\\\":\\\"" + document.nodeRef + "\\\",\\\"events\\\":\\\"task.complete\\\",\\\"taskTypes\\\": \\\"wfcf:confirmTask\\\"}\"}" />
<#assign records = recordsService.getRecordsForClass(query, clazz).records![] />

<#list records as record>
    <#assign type = record.taskType!"" />
    <#assign date = record.date!"" />
    <#assign comment = record.taskComment!"" />
    <#assign outcome = record.taskOutcomeTitle!"" />
    <#if record.taskPooledActors?? && record.taskPooledActors[0]??>
        <#assign role=record.taskPooledActors[0].authorityName!""
                 user=record.taskPooledActors[0].userName!"" />
    </#if>
<#-- User in taskPooledActors authority container is not available for now.
    Trying to get it from taskRole -->
    <#if !user?has_content && record.taskRole?has_content>
        <#assign user = record.taskRole!"" />
    </#if>
    <#assign allDecisions = allDecisions + [ {
        "date": date,
        "comment": comment,
        "outcome": outcome,
        "role": role!"",
        "user": user!""
    } ] />
</#list>

<#-- leave the latest decision for each role -->
<#assign decisionMap = {} />
<#list allDecisions?sort_by("date") as decision>
    <#assign decisionMap = decisionMap + { decision.role!decision.user : decision } />
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
        <td>${decision.date?string[dateFormat]}</td>
        <td><#if decision.role?has_content && people.getGroup(decision.role)??>${people.getGroup(decision.role).properties.authorityDisplayName!""}</#if></td>
        <td><#if decision.user?has_content && people.getPerson(decision.user)??>${people.getPerson(decision.user).properties.firstName!""} ${people.getPerson(decision.user).properties.lastName!""}</#if></td>
        <td>${decision.outcome}</td>
        <td>${decision.comment}</td>
        <td> &nbsp;</td>
    </tr>
</#list>
</table>

</body>
</html>
</#escape>