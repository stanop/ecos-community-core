<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<@forms.renderField field="prop_message" extension=extensions.controls.info />
	<#include "perform-response.ftl" />
</@>
