<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/workflow-general.ftl" />
	<div class="set">
		<div class="set-title">${msg('workflow.set.assignee')}</div>
		<@forms.renderField field="assoc_wfacf_confirmers" extension=extensions.controls.orgstruct />
	</div>
	<input id="${args.htmlid}_assoc_packageItems-cntrl-added" name="assoc_packageItems_added" type="hidden" value="${args.assoc_packageItems}" />
	<#include "../common/send-email-notifications.ftl" />
</@>
