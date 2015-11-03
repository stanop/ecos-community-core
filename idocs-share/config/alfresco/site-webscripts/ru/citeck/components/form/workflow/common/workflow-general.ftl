<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.general")}</div>
	<@forms.renderField field="prop_bpm_workflowDescription" extension=extensions.controls.textarea + {
		"label": msg("workflow.field.message")
	} />

	<div class="yui-g">
		<div class="yui-u first">
		<#if workflowDueDate?? >
			<@forms.renderField field="prop_bpm_workflowDueDate" extension = { 
				"label": msg("workflow.field.due"),
				"control" : {
					"template" : "/ru/citeck/components/form/controls/date.ftl",
					"params" : {
						"appendDaysToCurrentValue" : "${workflowDueDate}"
					}
				} 
			} />
		<#else>
			<@forms.renderField field="prop_bpm_workflowDueDate" extension = { 
				"label": msg("workflow.field.due")
			} />
		</#if>
		</div>
		<div class="yui-u">
			<#if customPriority?? >
				<@forms.renderField field="prop_bpm_workflowPriority" extension = {
					"label" : msg('workflow.field.priority'),
					"help" : "",
					"control" : {
						"template" : "/org/alfresco/components/form/controls/selectone.ftl",
						"params" : {
							"options" : "${customPriority}"
						}
					}
				} />
			<#else>
				<@forms.renderField field="prop_bpm_workflowPriority" extension=extensions.workflow.priority />
			</#if>
		</div>
	</div>

</div>
