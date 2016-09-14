<#import "/ru/citeck/views/views.lib.ftl" as views />

<#assign elementId = fieldId + "-" + template />

<@standalone>
	<@markup id="css" >
		<@link rel="stylesheet" href="${url.context}/res/citeck/components/invariants/invariants.css" group="region" />
	</@>

	<@markup id="html">
		<@renderContent />
	</@>
</@>

<#macro renderContent>
	<#assign template = template!"default" />
	<#assign template_exists = "ru.citeck.ecos.freemarker.TemplateExistsMethod"?new() />

	<div id="${elementId}" class="region single-region template-${template}">
		<#assign file>/ru/citeck/views/region/${template}.ftl</#assign>
		<#if template_exists(file)><#include file /><#return /></#if>

		<#assign file>/ru/citeck/views/region/default.ftl</#assign>
		<#if template_exists(file)><#include file /><#return /></#if>
	</div>
</#macro>