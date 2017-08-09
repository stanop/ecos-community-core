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
<#assign mandatoryConfirmersURL="Alfresco.constants.PROXY_URI + 'citeck/confirm/mandatory-confirmers?nodeRef=' + nodeRef" />

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
						var childProtocol = itemNode.childAssocs['meet:childProtocol'];
						if(!childProtocol || childProtocol.length == 0) return;
						Alfresco.util.Ajax.jsonGet({
							url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+childProtocol[0],
							successCallback: {
								fn: function(response) {
									var childProtocolNode = response.json;
									var confirmersControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_wfcf_confirmers-cntrl");
									if(confirmersControl) {
									if(childProtocolNode.assocs['meet:participants'])
									{
										confirmersControl.selectItems((childProtocolNode.assocs['meet:participants']||[]).join(','));
									}
									}
								}
							}
						});
					}
				}
			});
		});
	// ]]></script>

	<#include "/ru/citeck/components/form/workflow/common/workflow-general.ftl" />

	<#include "/ru/citeck/components/form/workflow/confirm/workflow-assignee.ftl" />

	<#include "/ru/citeck/components/form/workflow/common/task-items.ftl" />

	<#include "/ru/citeck/components/form/workflow/common/send-email-notifications.ftl" />
</@>
