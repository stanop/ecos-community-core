<input type="hidden" name="alf_assoctype" value="${args.assocType!}" />

<@forms.renderField field="prop_cm_name" />
<@forms.renderField field="prop_cm_title" extension = { "control": {
	"template": "/org/alfresco/components/form/controls/textfield.ftl",
	"params": {}
} } />
<@forms.renderField field="prop_icase_caseClass" extension = { "control": {
	"template" : "/ru/citeck/components/form/controls/dictionary/class.ftl",
	"params": {}
} } />
<@forms.renderField field="prop_icase_elementType" extension = { "control": {
	"template" : "/ru/citeck/components/form/controls/dictionary/class.ftl",
	"params": {}
} } />
<@forms.renderField field="prop_icase_copyElements" />
