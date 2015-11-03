<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

    <@forms.renderField field="prop_wfcf_confirmOutcome" extension = extensions.properties.readOnly  />

    <#include "../common/task-response.ftl" />

</@>
