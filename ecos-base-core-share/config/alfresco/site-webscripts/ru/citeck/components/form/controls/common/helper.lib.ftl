<#function getFieldValue field>
	<#if field.value?is_number>
		<#assign fieldValue=field.value?c>
	<#else>
		<#assign fieldValue=field.value?html>
	</#if>
	<#-- search for constraint -->
	<#assign labelSeparator = "|" />
	<#list form.constraints as constraint>
		<#if constraint.fieldId == field.id && constraint.id == "LIST">
			<#assign params = constraint.params />
			<#if params?is_string>
				<#assign params = params?eval />
			</#if>
			<#list params.allowedValues as value>
				<#assign parts = value?split(labelSeparator) />
				<#if parts[0] == field.value>
					<#assign fieldValue = parts[1]!parts[0] />
				</#if>
			</#list>
		</#if>
	</#list>
	<#return fieldValue />
</#function>
