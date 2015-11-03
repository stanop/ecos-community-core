<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
]/>

<@forms.fileUploadSupport />

<@forms.renderFormsRuntime formId=formId />

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>
    <input type="hidden" name="alf_assoctype" value="sys:children" />

    <@forms.renderField field="prop_cm_title" />

    <@forms.renderField field="assoc_journal_destination" />

    <@forms.renderField field="prop_journal_type"  extension = { "control": {
        "template": "/ru/citeck/components/form/controls/dictionary/type.ftl",
        "params": {
            "valueFieldName": "prefixedName"
        }
    } } />

    <@forms.renderField field="prop_journal_formId"/>

    <@forms.renderField field="prop_journal_isDefault" />
</@>
