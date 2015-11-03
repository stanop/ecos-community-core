<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

<#include "commonSettings.ftl" />

<@forms.renderField field="prop_icase_elementKey" extension = { "control": {
	"template" : "/ru/citeck/components/form/controls/dictionary/property.ftl",
	"params": {}
} } />
<@forms.renderField field="prop_icase_caseKey" extension = { "control": {
	"template" : "/ru/citeck/components/form/controls/dictionary/property.ftl",
	"params": {}
} } />

<#-- TODO include subcase settings when behaviours are supported -->
<#-- <#include "subcaseSettings.ftl" /> -->

</@>
