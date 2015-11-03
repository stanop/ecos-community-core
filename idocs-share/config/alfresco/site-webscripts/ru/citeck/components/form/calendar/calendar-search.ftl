<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
]/>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<@forms.renderField field="prop_ia_whatEvent" extension = extensions.search.text />

<@forms.renderField field="prop_ia_whereEvent" extension = extensions.search.text />

<@forms.renderField field="prop_ia_descriptionEvent" extension = extensions.search.text />

<@forms.renderField field="prop_ia_fromDate" extension = extensions.search.date />

<@forms.renderField field="prop_ia_toDate" extension = extensions.search.date />

</@>
