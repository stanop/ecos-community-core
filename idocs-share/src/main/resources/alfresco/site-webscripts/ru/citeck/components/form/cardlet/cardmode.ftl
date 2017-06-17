<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@forms.renderField field="prop_cardlet_cardModeId" />

<@forms.renderField field="prop_cm_title" />
<@forms.renderField field="prop_cm_description" />

<@forms.renderField field="prop_cardlet_cardModeOrder" />

<@forms.renderField field="prop_cardlet_allowedType" />

<@forms.renderField field="prop_cardlet_allowedAuthorities" />

<@forms.renderField field="prop_cardlet_condition" extension = extensions.controls.textarea />



</@>
