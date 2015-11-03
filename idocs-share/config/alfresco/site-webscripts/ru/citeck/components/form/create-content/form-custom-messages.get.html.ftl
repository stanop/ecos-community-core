<#assign headPrefix = 'create.content.header.' />
<#assign failPrefix = 'create.content.fail.' />
<#assign itemId = (args.itemId!"_")?replace(":","_") />
<#assign formId = (args.formId!"_") + "." />
<#function getMessage codes>
	<#list codes as code>
		<#assign message = msg(code) />
		<#if message != code>
			<#return message />
		</#if>
	</#list>
	<#return '' />
</#function>

<#assign headMsg = getMessage([headPrefix + formId + itemId, headPrefix + itemId]) />
<#if headMsg != "">
	<script type="text/javascript">
		jQuery('.create-content-mgr .heading').text("${headMsg}");
	</script>
</#if>

<#-- todo set failMsg in the future -->
<#assign failMsg = getMessage([failPrefix + formId + itemId, failPrefix + itemId]) />
