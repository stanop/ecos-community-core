<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
    <#include "../common/task-info.ftl" />
    <#include "../common/task-progress.ftl" />
    <#include "../common/task-items.ftl" />

<div class="set">
    <div class="set-title">${msg("workflow.set.response")}</div>

    <@forms.renderField field="prop_cwf_assignDate"  extension = { "control": {
    "template": "/org/alfresco/components/form/controls/info.ftl"
    }} />

    <@forms.renderField field="prop_cwf_lastcomment"  extension = { "control": {
    "template": "/org/alfresco/components/form/controls/info.ftl"
    }} />

    <#include "../common/task-response.ftl" />

</div>

</@>