<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@forms.setMandatoryFields
fieldNames = [
"assoc_packageItems"
]/>

<@formLib.renderFormContainer formId=formId>

    <#include "../common/workflow-general.ftl" />

    <div class="set">
        <div class="set-title">${msg('workflow.set.assignee')}</div>
            <@forms.renderField field="assoc_wfcr_corrector" extension = extensions.controls.orgstruct />
    </div>

    <#include "../common/task-items.ftl" />

    <#include "../common/send-email-notifications.ftl" />

</@>