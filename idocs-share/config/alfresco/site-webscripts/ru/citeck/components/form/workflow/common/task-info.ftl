<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<div class="set">
	<div class="set-title">${msg("workflow.set.general")}</div>
	<@forms.renderField field="prop_message" extension=extensions.controls.info />

	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field="prop_taskOwner" />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_cwf_sender" extension=extensions.controls.userName />
		</div>
	</div>
	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field="prop_bpm_dueDate" extension=extensions.properties.readOnly />
		</div>
		<div class="yui-u">
			<@forms.renderField field="prop_bpm_priority" extension=extensions.workflow.priority + extensions.properties.readOnly />
		</div>
	</div>

</div>
