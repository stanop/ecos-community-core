<@forms.renderField field="prop_icase_createSubcase" />
<@forms.displayConditional "prop_icase_createSubcase" "true">
	<@forms.renderField field="prop_icase_removeSubcase" />
	<@forms.displayConditional "prop_icase_removeSubcase" "false">
		<@forms.renderField field="prop_icase_removeEmptySubcase" />
	</@>
	<@forms.renderField field="prop_icase_subcaseType" extension = { "control": {
		"template" : "/ru/citeck/components/form/controls/dictionary/type.ftl",
		"params": {}
	} } />
	<@forms.renderField field="prop_icase_subcaseAssoc" extension = { "control": {
		"template" : "/ru/citeck/components/form/controls/dictionary/association.ftl",
		"params": {}
	} } />
</@>

