<html>
<head>
<title>Workflow details for workflow ${workflowInstance.id}</title>
</head>
<body>

Workflow decisions:
<#assign package = workflowInstance.package />
<#assign packageItems = package.childAssocs["wfcf:confirmDecisions"]![] />
<table style="width: 100%; border: solid 1px black;">
	<tr>
		<td>Date</td>
		<td>Role</td>
		<td>Person</td>
		<td>Decision</td>
		<td>Comment</td>
	</tr>
<#list packageItems as item>
	<#assign task = workflow.getTaskById(item.properties["wfcf:confirmTaskId"]) />
	<#assign outcomeProperty = task.properties["bpm:outcomePropertyName"]!"bpm:outcome" />
	<#assign role = "" />
	<#if task.properties["bpm:pooledActors"]?? && task.properties["bpm:pooledActors"]?size != 0>
		<#assign role = task.properties["bpm:pooledActors"][0].properties["cm:authorityDisplayName"] />
	</#if>
	
	<tr>
		<td>${task.properties["bpm:completionDate"]?string("dd.MM.yyyy HH:mm")}</td>
		<td>${role}</td>
		<td>${task.properties["cm:owner"]!"no-owner"}</td>
		<td>${task.properties[outcomeProperty]!"no-outcome"}</td>
		<td>${task.properties["bpm:comment"]!"no-comment"}</td>
	</tr>
</#list>
</table>

</body>
</html>