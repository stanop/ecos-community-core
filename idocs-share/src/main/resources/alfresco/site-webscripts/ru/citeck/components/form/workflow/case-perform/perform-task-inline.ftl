<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<div style="float: left">
		<@forms.renderField field="prop_bpm_description" extension=extensions.controls.info />
	</div>
	<#if form.fields["prop_cwf_isOptionalTask"].value><span>${msg("workflowtask.field.cwf_isOptionalTask")}</span></#if>

	<@forms.renderField field="prop_bpm_comment" extension=extensions.controls.textarea + {
		"label": msg("workflow.field.comment")
	} />

	<@forms.renderField field="prop_wfcp_performOutcome" extension = { "control": {
		"template": "/ru/citeck/components/form/workflow/case-perform/perform-outcomes.ftl"
	}} />
</@>


