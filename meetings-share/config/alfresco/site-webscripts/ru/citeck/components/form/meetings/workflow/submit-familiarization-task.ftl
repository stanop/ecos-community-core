<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"prop_bpm_workflowDueDate",
	"assoc_packageItems"
] />
<#-- <#assign workflowDueDate=3 /> -->
<#assign allowAddAction = false />
<#assign allowRemoveAction = false />
<#assign allowRemoveAllAction = false />
<#assign allowViewMoreAction = false />

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	<script type="text/javascript">// <![CDATA[
		YAHOO.Bubbling.on("renderCurrentValue", function(layer, args) {
			var control = args[1].eventGroup;
			if(control.id != "${args.htmlid}_assoc_packageItems-cntrl") return;
			var currentItems = control.getSelectedItems();
			if(currentItems.length == 0) return;
			Alfresco.util.Ajax.jsonGet({
				url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+currentItems[0],
				successCallback: {
					fn: function(response) {
						var itemNode = response.json;
						var childAgenda = itemNode.childAssocs['meet:childAgenda'];
						if(!childAgenda || childAgenda.length == 0) return;
						Alfresco.util.Ajax.jsonGet({
							url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+childAgenda[0],
							successCallback: {
								fn: function(response) {
									var childAgendaNode = response.json;
									var wfgfam_peopleControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_wfgfam_people-cntrl");
									if(wfgfam_peopleControl) {
									if(childAgendaNode.assocs['meet:plannedParticipants'])
									{
										wfgfam_peopleControl.selectItems((childAgendaNode.assocs['meet:plannedParticipants']||[]).join(','));
									}
									}
								}
							}
						});
						var dateWhen = itemNode.props['meet:when'];
						var bpm_workflowDueDateControl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_bpm_workflowDueDate-cntrl");
						var fieldDate = "${args.htmlid}_prop_bpm_workflowDueDate-cntrl-date";
						var pickerField = "${args.htmlid}_prop_bpm_workflowDueDate-cntrl";
						var fieldDateFull = "${args.htmlid}_prop_bpm_workflowDueDate";
						var picker = Alfresco.util.ComponentManager.get(pickerField);
						var dateDesrc = Dom.get(fieldDate);
						var dateFullDesrc = Dom.get(fieldDateFull);
						if(dateDesrc && dateFullDesrc)
						{
							if(dateWhen)
							{
								var dateObj = new Date(dateWhen);
								dateDesrc.value = dateObj.toString(picker._msg("form.control.date-picker.entry.date.format"));
							   // dateObj.getDate()+"/"+ (dateObj.getMonth() + 1)+"/"+dateObj.getFullYear();
								
								dateFullDesrc.value = Alfresco.util.toISO8601(dateObj, {"milliseconds":true});
							}
							else
							{
								dateDesrc.value = "";
								dateFullDesrc.value = "";
							}
						}
						
					}
				}
			});
		});

	// ]]></script>

	<#include "/ru/citeck/components/form/workflow/common/workflow-general.ftl" />

<div class="set">
	<div class="set-title">${msg("workflow.set.assignee")}</div>
		<@forms.renderField field="assoc_wfgfam_people" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
				}
			}
		}/>
</div>

	<#include "/ru/citeck/components/form/workflow/common/task-items.ftl" />

	<#include "/ru/citeck/components/form/workflow/common/send-email-notifications.ftl" />
</@>
