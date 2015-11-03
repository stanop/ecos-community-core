<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.fileUploadSupport />

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<@formLib.renderFormContainer formId=formId>
	<#list form.structure as item>
		<#if item.kind == "set">
			<@formLib.renderSet set=item />
		<#else>
			<@formLib.renderField field=form.fields[item.id] />
		</#if>
	</#list>
</@>