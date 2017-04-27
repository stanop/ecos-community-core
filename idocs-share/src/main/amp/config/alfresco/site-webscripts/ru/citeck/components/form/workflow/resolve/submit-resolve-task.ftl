<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<div class="set">
		<div class="set-title">${msg('workflow.set.manager')}</div>
		<@forms.renderField field="assoc_wfres_resolver" extension=extensions.controls.orgstruct />
	</div>
	<#include "resolution.ftl" />
	<#include "../common/task-items.ftl" />
</@>
