<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields fieldNames = [ "assoc_wfperf_controller" ] 
	condition = "prop_wfperf_enableControl == 'true'" />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/workflow-general.ftl" />
	<div class="set">
		<div class="set-title">${msg('workflow.set.assignees')}</div>
		<@forms.renderField field="assoc_wfperf_performers" extension=extensions.controls.orgstruct />
		
		<@forms.renderField field="prop_wfperf_enableControl" />
		<@forms.displayConditional "prop_wfperf_enableControl" "true">
			<@forms.renderField field="assoc_wfperf_controller"  extension = {
				"endpointType": "cm:person",
				"control": {
					"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
					"params": {
						"defaultUserName": "${(user.id)?js_string}"
					}
				} 
			}/>
		</@>
	</div>
	<#include "../common/task-items.ftl" />
</@>
