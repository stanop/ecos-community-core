<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<@formLib.renderFormContainer formId=formId>

<input type="hidden" name="alf_assoctype" value="sys:children" />

<@forms.renderField field="assoc_route_participants" />

</@>
