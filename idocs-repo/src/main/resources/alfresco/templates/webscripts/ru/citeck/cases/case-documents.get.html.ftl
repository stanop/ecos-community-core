<#macro renderContainerOptions container>
	<option value="${container.node.nodeRef}">${container.node.name}</option>
	<#list container.containers as item>
	<@renderContainerOptions item />
	</#list>
</#macro>


<html>
<head>
	<title>case-documents upload test</title>
<body>

<form method="post" action="" enctype="multipart/form-data">

<p>
	<label for="filedata">File:</label>
	<input id="filedata" name="filedata" type="file" />
</p>
<p>
	<label for="container">Container:</label>
	<select id="container" name="container">
		<@renderContainerOptions container />
	</select>
</p>
<p>
	<label for="type">Document type:</label>
	<select id="type" name="type">
		<#list documentTypes as type>
		<option value="${type.nodeRef}">${type.name}</option>
		</#list>
	</select>
</p>
<p>
	<label for="kind">Document kind:</label>
	<select id="kind" name="kind">
		<#list documentKinds as kind>
		<option value="${kind.nodeRef}">${kind.name}</option>
		</#list>
	</select>
</p>
<p>
	<label for="multiple">Multiple documents allowed:</label>
	<select id="multiple" name="multiple">
		<option value="false">false</option>
		<option value="true">true</option>
	</select>
</p>
<p>
	<input type="submit" value="Submit" />
</p>

</form>

</body>
</html>