<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<#assign mandatoryConfirmersURL="Alfresco.constants.PROXY_URI + 'citeck/confirm/mandatory-confirmers?nodeRef=' + nodeRef" />

<@formLib.renderFormContainer formId=formId>
	<#include "../common/workflow-info.ftl" />
	<#include "workflow-mandatory-assignee.ftl" />
	<#include "../common/task-items.ftl" />
</@>
