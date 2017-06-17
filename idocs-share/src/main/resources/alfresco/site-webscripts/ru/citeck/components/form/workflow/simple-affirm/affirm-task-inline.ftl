<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<@forms.renderField field = "prop_wfsa_affirmDate"/>
	
	<@forms.renderField field = "prop_bpm_comment" extension = {
		"label": "Комментировать",
		"control": {
			"template": "/org/alfresco/components/form/controls/textarea.ftl",
			"params": {}
		}
	} />

	<#assign outcomes = "Affirmed|" + msg("affirm-task.outcome.Affirmed") + "#alf#Declined|" + msg("affirm-task.outcome.Declined") />

	<@forms.renderField field = "prop_wfsa_affirmOutcome" extension = { "control": {
		"template": "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl",
		"params": {
			"options": outcomes
		}
	} } />
	
</@>
