<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#if formUI == "true">
    <@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
    <#assign twoColumnClass = "yui-g" />
    <#assign threeColumnClass = "yui-gb" />
<#else>
    <#assign twoColumnClass = "yui-g" />
    <#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_cm_firstName" />
        <@forms.renderField field="prop_cm_lastName" />
        <@forms.renderField field="prop_cm_middleName"/>
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_cm_userName" />
    </div>
</div>

        <@forms.renderField field="prop_idocs_nameInGenitiveCase" />

<div class="${twoColumnClass}">
    <div class="yui-u first">
        <@forms.renderField field="prop_cm_organization" />
        <@forms.renderField field="prop_cm_jobtitle" />
    </div>
    <div class="yui-u">
        <@forms.renderField field="prop_cm_telephone" />
        <@forms.renderField field="prop_cm_email" />
    </div>
</div>

    <@forms.renderField field="prop_delegate_available"  extension = {
    "tepmlate": "/ru/citeck/components/form/controls/checkbox-checked-default.ftl"
    }/>

    <@forms.renderField field="prop_cm_userStatus" />
    <@forms.renderField field="prop_uc_preset" extension = {
    "tepmlate": "/ru/citeck/components/form/controls/select.ftl",
    "params":{
    "optionsUrl":"/share/service/citeck/presets/user.json",
    "titleField":"name",
    "valueField":"id",
    "resultsList":"presets"
    }
    } />

</@>