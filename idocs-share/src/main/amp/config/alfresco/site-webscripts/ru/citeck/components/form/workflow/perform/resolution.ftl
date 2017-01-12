<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.task-description")}</div>
	<@forms.renderField field="prop_wfperf_taskDescription" extension=extensions.controls.textarea />

	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field="prop_wfperf_taskDueDate" />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_wfperf_taskPriority" extension=extensions.workflow.priority />
		</div>
	</div>

</div>
