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

<@forms.renderField field="prop_meet_meetingNumber" extension = extensions.search.text />

<@forms.renderField field="assoc_meet_initiator" extension = {
	"control": {
		"template": "/ru/citeck/components/form/controls/association_search.ftl",
		"params": {
			"searchWholeRepo": "true"
		}
	}
} />

<@forms.renderField field="prop_meet_where" extension = extensions.search.text />

<@forms.renderField field="prop_meet_when" extension = extensions.search.date />

<@forms.renderField field="prop_meet_subject" extension = extensions.search.text />

</@>
