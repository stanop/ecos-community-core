<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/task-info.ftl" />
	<#if !(form.data.wfcf_canConfirmWithComments!false)>
		<#assign outcomes = [ "Confirmed", "Reject" ] />
	</#if>
	<#include "../common/task-items.ftl" />
	<#include "../common/task-response.ftl" />
</@>
