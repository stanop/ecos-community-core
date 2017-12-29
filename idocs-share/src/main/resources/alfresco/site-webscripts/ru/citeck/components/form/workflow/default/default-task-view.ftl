<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<#include "../common/workflow-info.ftl" />

	<#include "../common/task-items.ftl" />

</@>
