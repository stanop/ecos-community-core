<html>
<head>
<title></title>
<#assign contractor = document.assocs["dms:agreement_to_contractor"][0] />
</head>
<body>

<h1>Лист согласования</h1>
<p>
	договора №${document.properties["dms:number"]!"б-н"}
	c ${contractor.properties["dms:juridicalTitle"]}
</p>

<table>

	<tr>
		<td>Должность</td>
		<td>Согласующее лицо</td>
		<td>Решение</td>
		<td>Комментарий</td>
		<td>Дата</td>
	</tr>

	<#list tasks as task>
	<tr>
		<td>${task.role.properties["cm:displayName"]}</td>
		<td>${task.assignee.properties["cm:firstName"]} ${task.assignee.properties["cm:lastName"]}</td>
		<td>${task.outcome}</td>
		<td>${task.comment}</td>
		<td>${task.date?datetime?string("dd.MM.yyyy hh:mm")}</td>
	</tr>
	</#list>

</table>

</body>
</html>