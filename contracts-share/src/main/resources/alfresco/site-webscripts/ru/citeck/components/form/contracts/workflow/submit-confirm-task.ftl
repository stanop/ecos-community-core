<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
"assoc_wfcf_confirmers"
]/>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>


    <#include "/ru/citeck/components/form/workflow/common/workflow-info.ftl" />
    <#include "/ru/citeck/components/form/workflow/confirm/workflow-assignee-with-routes.ftl" />
    <#include "/ru/citeck/components/form/workflow/common/task-items.ftl" />

<div class="set">
    <div class="set-title">${msg("workflow.set.other")}</div>
    <@forms.renderField field="prop_cwf_hasContractorApproval" extension = {
    "value":false,
    "control" : {
    "template" : "/org/alfresco/components/form/controls/checkbox.ftl",
    "params":{
    }
    }} />
</div>

</@>
