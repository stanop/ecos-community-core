<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"prop_meet_question"
]/>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "create" >
	<input id="alf_assoctype" value="meet:childQuestions" class="hidden" />
</#if>

<@forms.renderField field="prop_meet_question" extension = extensions.controls.textarea />

<@forms.renderField field="assoc_meet_plannedReporters" extension = {
	"control": {
		"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
		"params": {
			"searchQuery" : "user=true&default=false"
		}
	}
} />

</@>
