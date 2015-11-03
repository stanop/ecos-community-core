<@markup id="shareConstants">
	<@inlineScript group="template-common">
		Alfresco.constants.URI_TEMPLATES = YAHOO.lang.merge(Alfresco.constants.URI_TEMPLATES, {
			<#list tokens?keys as key>
			"${key?js_string}": "${tokens[key]?js_string}"<#if key_has_next>,</#if>
			</#list>
		});
	</@>
</@>
