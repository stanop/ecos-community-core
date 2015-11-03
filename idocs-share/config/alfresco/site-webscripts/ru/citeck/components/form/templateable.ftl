<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
]/>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

	<@forms.renderField field="prop_dms_updateContent" extension = { 
		"control": {
			"template": "/ru/citeck/components/form/controls/always-hidden-textfield.ftl",
			"params": {
				"value": "true"
			}
		}
	} />

	<#if form.fields?size == 1 >
		<div class="invite" style="padding-bottom:1em;">${msg("form.templateable.generating.invite")}</div>
	<#else>
		<#assign keys = form.fields?keys>
		<#list keys as key>
			<#assign field = form.fields[key]>
			<#if field.id != "prop_dms_updateContent">
				<@forms.renderField field=field.name />
			</#if>
		</#list>
	</#if>

</@>
