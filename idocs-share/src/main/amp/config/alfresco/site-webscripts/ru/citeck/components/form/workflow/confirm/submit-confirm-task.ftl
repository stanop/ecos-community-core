<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
    "assoc_wfcf_confirmers"
]/>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<#include "../common/workflow-info.ftl" />
	<#include "workflow-assignee-with-routes.ftl" />
	<#include "../common/task-items.ftl" />

</@>
