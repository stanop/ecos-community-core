<html>
<head>
<style>
table, tr, td
{
border: 1px solid;
border-collapse: collapse;
overflow-wrap: break-word;
}
thead 
{
font-weight: bold;
}
</style>
</head>
<body>
<table style="width: 100%">
	<thead>
		<tr>
			<td>id</td>
			<td>version</td>
			<td>install date</td>
			<#if (args.details!"") == "true">
			<td>title</td>
			<td>description</td>
			<td>install state</td>
			<td>aliases</td>
			<td>dependencies</td>
			<td>editions</td>
			</#if>
		</tr>
	</thead>
	<tbody>
	<#list modules?sort_by('id') as module>
		<tr>
			<td>${module.id}</td>
			<td>${module.version.toString()}</td>
			<td><#if installDate??>"${module.installDate?string("yyyy-MM-dd")}"</#if></td>
			<#if (args.details!"") == "true">
			<td>${module.title!}</td>
			<td>${module.description!}</td>
			<td>${module.installState.toString()}</td>
			<td>
			<#list module.aliases![] as alias>
				${alias}<#if alias_has_next>, </#if>
			</#list>
			</td>
			<td>
			<#list module.dependencies![] as dependency>
				${dependency.dependencyId}: ${dependency.versionString}<#if dependency_has_next>, </#if>
			</#list>
			</td>
			<td>
			<#list module.editions![] as edition>
				${edition}<#if edition_has_next>, </#if>
			</#list>
			</td>
			</#if>
		</tr>
	</#list>
	</tbody>
<table>
</body>
</html>