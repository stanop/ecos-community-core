<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"assoc_meet_initiator",
	"prop_meet_where",
	"prop_meet_when"
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

<#if form.mode == "create">
	<@forms.formConfirmSupport formId=formId message="" />
</#if>

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "view" >
	<@forms.renderField field="prop_meet_meetingNumber" />
</#if>

<#if form.mode == "view" >
	<@forms.renderField field="prop_idocs_documentStatus" />
</#if>

<@forms.renderField field="assoc_meet_initiator" extension = {
	"control": {
		"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
		"params": {
			"searchQuery" : "user=true&default=false",
			"defaultUserName": "${(user.id)?js_string}"
		}
	}
} />

<@forms.renderField field="prop_meet_where" extension = extensions.controls.textarea />

<@forms.renderField field="prop_meet_when" extension = {
	"control": {
		"template": "/ru/citeck/components/form/controls/date.ftl",
		"params": {
			"showTime" : "true"
		}
	}
} />

<@forms.renderField field="prop_meet_subject" extension = extensions.controls.textarea />

<#if form.mode == "view" >
	<@forms.renderField field="prop_cm_created" />
</#if>

</@>
