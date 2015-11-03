<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.task.progress")}</div>
	<@forms.renderField field="prop_bpm_status" />
</div>
