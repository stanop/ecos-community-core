<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/workflow-general.ftl" />
	<div class="set">
		<div class="set-title">${msg('workflow.set.assignee')}</div>
		<@forms.renderField field="assoc_wfsa_affirmAgent" extension=extensions.controls.orgstruct />
	</div>
	<#include "../common/task-items.ftl" />
</@>
