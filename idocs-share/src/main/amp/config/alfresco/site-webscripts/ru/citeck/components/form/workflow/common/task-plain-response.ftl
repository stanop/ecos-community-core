<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderField field="prop_bpm_comment" extension=extensions.controls.textarea + {
	"label": msg("workflow.field.comment")
} />

<#assign outcomeField = forms.findOutcomeField(form.fields)!{} />
<#if outcomeField.id??>
	<#if !(outcomes??)>
		<#assign outcomes = forms.parseOutcomes(outcomeField.control.params.options) />
	</#if>
	<#if !(outcomeLabels??)>
		<#assign outcomeLabels = forms.parseOutcomeLabels(outcomeField.control.params.options) />
	</#if>
	<@formLib.renderField field = outcomeField + {
		"control": {
			"template": "/ru/citeck/components/form/controls/workflow/activiti-transitions.ftl",
			"params": outcomeField.control.params + {
				"options": forms.constructOutcomeOptions(outcomes, outcomeLabels)
			}
		}
	} />
</#if>
