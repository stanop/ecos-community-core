<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<@forms.renderField field = "prop_wfsp_extendDate"/>
	
	<@forms.renderField field = "prop_bpm_comment" extension = {
		"control": {
			"template": "/org/alfresco/components/form/controls/textarea.ftl",
			"params": {}
		}
	} />
    <@forms.renderField field = "prop_wfsp_paymentOutcome" extension = { "control": {
		"template": "/ru/citeck/components/form/controls/workflow/activiti-transitions.ftl",
		"params": {
			"options": "Paid|payment-task.outcome.Paid#alf#Extend|payment-task.outcome.Extend"
		}
    } } />

</@>
