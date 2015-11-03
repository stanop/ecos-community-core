<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.general")}</div>

	<@forms.renderField field="prop_bpm_workflowDescription" extension=extensions.controls.textarea + {
	"label": msg("workflow.field.message")
	} />

	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field="prop_bpm_workflowDueDate" />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_bpm_workflowPriority" extension=extensions.workflow.priority />
		</div>
	</div>

</div>
