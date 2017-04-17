<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<div style="float: left">
		<@forms.renderField field="prop_bpm_description" extension=extensions.controls.info />
	</div>
	<#if form.fields["prop_cwf_isOptionalTask"].value><span>${msg("workflowtask.field.cwf_isOptionalTask")}</span></#if>

	<#if form.fields["prop_wfcp_performOutcomes"]?? && form.fields["prop_wfcp_performOutcomes"].value?has_content>
		<#assign customOutcomes = form.fields["prop_wfcp_performOutcomes"].value />
		<#if !(outcomes??)>
			<#assign outcomes = forms.parseOutcomes(customOutcomes) />
		</#if>
		<#if !(outcomeLabels??)>
			<#assign defaultOutcomes = (forms.findOutcomeField(form.fields).control.params.options)!"" />
			<#assign outcomeLabels = parseOutcomeLabels(customOutcomes, defaultOutcomes) />
		</#if>
	</#if>

	<#include "../common/task-plain-response.ftl" />
</@>

<#function parseOutcomeLabels customOptions defaultOptions>
	<#if defaultOptions?has_content>
	    <#assign defaultLabels = forms.parseOutcomeLabels(defaultOptions) />
	<#else>
		<#assign defaultLabels = {} />
	</#if>
	<#assign outcomeOptions = customOptions?split("#alf#") />
	<#assign outcomeLabels = {} />
	<#list outcomeOptions as option>
		<#assign keyValue = option?split('|') />
		<#assign key = keyValue[0] />
		<#if keyValue?size == 2>
		    <#assign value = keyValue[1] />
		<#elseif defaultLabels[key]??>
			<#assign value = defaultLabels[key] />
		<#else>
			<#assign value = key />
		</#if>
		<#assign outcomeLabels = outcomeLabels + {
			key : value
		} />
	</#list>
	<#return outcomeLabels />
</#function>
