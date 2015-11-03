<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.resolution")}</div>
	<@forms.renderField field="prop_wfres_resolutionText" extension=extensions.controls.textarea />

	<div class="yui-gb">
		<div class="yui-u first">
			<@forms.renderField field="prop_wfres_resolutionDueDate" />
		</div>
		<div class="yui-u">
			<@forms.renderField field="assoc_wfres_resolutionPerformers" extension=extensions.controls.orgstruct />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_wfres_resolutionPriority" extension=extensions.workflow.priority />
		</div>
	</div>

</div>
