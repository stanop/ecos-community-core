<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<@forms.renderField field="prop_message" extension=extensions.controls.info />
	<#if !(form.data.wfcf_canConfirmWithComments!false)>
		<#assign outcomes = [ "Confirmed", "Reject" ] />
	</#if>
	<#include "../common/task-plain-response.ftl" />
</@>
