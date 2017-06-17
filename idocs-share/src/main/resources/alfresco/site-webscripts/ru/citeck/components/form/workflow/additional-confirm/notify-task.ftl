<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/task-info.ftl" />
	<#include "../common/task-items.ftl" />

	<#assign outcomeLocalization = {
		"Confirmed": "Согласовано",
		"Reject": "НЕ согласовано"
	} />
	<@forms.renderField field = "prop_wfacf_confirmOutcome" extension = extensions.controls.info + {
		"label": "Решение отправителя",
		"value": outcomeLocalization[form.data.prop_wfacf_confirmOutcome]!"Unknown"
	} />

	<#include "../common/task-response.ftl" />
</@>
