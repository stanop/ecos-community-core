<#-- disable all mandatory constraints -->
<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.suppressMandatoryConstraints />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<#list form.structure as item>
		<#if item.kind == "set">
			<@formLib.renderSet set=item />
		<#else>
			<@formLib.renderField field=form.fields[item.id] />
		</#if>
	</#list>
</@>
