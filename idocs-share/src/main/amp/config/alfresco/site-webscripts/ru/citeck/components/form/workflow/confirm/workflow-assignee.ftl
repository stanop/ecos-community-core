<div class="set">
	<div class="set-title">${msg("workflow.set.assignee")}</div>
	<input name="prop_wfcf_hasMandatoryConfirmers" value="false" type="hidden" />

	<@forms.renderField field = "assoc_wfcf_confirmers" extension = { "control" : {
		"template" : "/ru/citeck/components/form/controls/orgstruct-select.ftl",
		"params" : {
		}
	} } />
	<@forms.renderField field = "prop_wfcf_precedence" extension = { "control" : {
		"template" : "/ru/citeck/components/form/controls/groupable.ftl",
		"params" : {
			"field" : "assoc_wfcf_confirmers",
			"itemSelector" : "> * > * > *"
		}
	} } />
	
</div>
