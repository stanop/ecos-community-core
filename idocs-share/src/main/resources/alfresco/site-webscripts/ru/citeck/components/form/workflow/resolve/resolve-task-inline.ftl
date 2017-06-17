<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields fieldNames = [ "prop_wfres_resolutionText", "assoc_wfres_resolutionPerformers" ] 
	condition = "prop_wfres_resolveOutcome == 'ToPerform'" />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "resolution.ftl" />
	<#include "../common/task-response.ftl" />
</@>
