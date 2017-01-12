<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<@forms.renderField field="prop_bpm_comment" extension=extensions.controls.textarea />
	<#assign outcomeField = form.fields['prop_wfscan_scanOutcome'] />
	<@formLib.renderField field = outcomeField + {
		"control": {
			"template": "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl",
			"params": outcomeField.control.params
		}
	} />
	<div id="onActionUploadNewVersion">
	  Необходимо приложить скан-копию действием 
	    <a class="action-link" style="text-decoration: underline; cursor: pointer;">Загрузить новую версию</a>
	</div>
</@>
