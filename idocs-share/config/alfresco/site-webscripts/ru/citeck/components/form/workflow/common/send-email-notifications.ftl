<div class="set">
	<div class="set-title">${msg("workflow.set.other")}</div>
	<@forms.renderField field = "prop_cwf_sendNotification" extension = { "control" : {
		"template" : "/org/alfresco/components/form/controls/workflow/email-notification.ftl",
		"params" : {
		}
	} } />
</div>
