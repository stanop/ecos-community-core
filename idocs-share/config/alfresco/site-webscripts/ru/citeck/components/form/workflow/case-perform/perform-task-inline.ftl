<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<div style="float: left">
		<@forms.renderField field="prop_bpm_description" extension=extensions.controls.info />
	</div>
	<#if form.fields["prop_cwf_isOptionalTask"].value><span>${msg("workflowtask.field.cwf_isOptionalTask")}</span></#if>
	<#include "../common/task-plain-response.ftl" />
</@>
