<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
"assoc_wfcf_confirmers",
"assoc_packageItems"

]/>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

    <#include "../common/workflow-general.ftl" />
    <@forms.renderField field="prop_cwf_workflowStartDate" />

    <div class="set">
        <div class="set-title">${msg('workflow.set.assignee')}</div>
        <@forms.renderField field="assoc_wfcf_confirmers" extension=extensions.controls.orgstruct />
    </div>

    <#--div class="set">
        <@forms.renderField field="assoc_wfcf_subscribers" extension= {
        "label":"На ознакомление",
        "control": {
        "template": "/org/alfresco/components/form/controls/authority.ftl",
        "params": {
        }
        }
        }/>
    </div-->

    <#include "../common/task-items.ftl" />
    <#include "../common/send-email-notifications.ftl" />

</@>