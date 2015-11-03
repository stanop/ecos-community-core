<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

<#include "commonSettings.ftl" />

<@forms.renderField field="prop_icase_assocName" extension = { "control": {
	"template" : "/ru/citeck/components/form/controls/dictionary/association.ftl",
	"params": {}
} } />
<@forms.renderField field="prop_icase_assocType" />

<#include "subcaseSettings.ftl" />

</@>
