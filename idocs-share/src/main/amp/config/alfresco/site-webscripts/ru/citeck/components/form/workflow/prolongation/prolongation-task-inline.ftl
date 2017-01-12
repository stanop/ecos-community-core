<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<@forms.renderField field = "prop_wfprolong_prolongationDate"/>
	
	<@forms.renderField field = "prop_bpm_comment" extension = {
		"control": {
			"template": "/org/alfresco/components/form/controls/textarea.ftl",
			"params": {}
		}
	} />
	
    <@forms.renderField field = "prop_wfprolong_prolongationOutcome" extension = { "control": {
		"template": "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl",
		"params": {
			"options": "Prolongate|Продлить#alf#SendToArchive|Списать в архив"
		}
    } } />

</@>
