<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "resolution.ftl" />
	<#include "../common/task-response.ftl" />
</@>
