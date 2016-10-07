<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields fieldNames = [
	"prop_bpm_workflowDueDate",
	"assoc_wfperf_controller"
]/>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>

<div class="set">
	<div class="set-title">${msg("workflow.set.general")}</div>
	<@forms.renderField field="prop_bpm_workflowDescription" extension=extensions.controls.textarea + {
		"label": msg("workflow.submit-perform-task.assignment")
	} />

	<div class="yui-g">
		<div class="yui-u first">
		<#if workflowDueDate?? >
			<@forms.renderField field="prop_bpm_workflowDueDate" extension = { 
			"label": msg("workflow.submit-perform-task.due"),
			"control" : {
				"template" : "/ru/citeck/components/form/controls/date.ftl",
				"params" : {
					"showTime" : "true",
					"appendDaysToCurrentValue" : "${workflowDueDate}"
				}
			} } />
		<#else>
			<@forms.renderField field="prop_bpm_workflowDueDate"  extension = {
				"label": msg("workflow.submit-perform-task.due"),
				"control": {
					"template": "/ru/citeck/components/form/controls/date.ftl",
					"params": {
						"showTime" : "true"
					}
				}
			} />
		</#if>
		</div>
	</div>
</div>

<div class="set">
	<div class="set-title">${msg('workflow.set.assignees')}</div>
	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field="assoc_wfperf_performers" extension=extensions.controls.orgstruct + {
				"label": msg("workflow.submit-perform-task.assignedPerformer")
			} />
		</div>
		<div class="yui-u">
            <@forms.renderField field="assoc_wfperf_coperformers" extension=extensions.controls.orgstruct + {
                "label": msg("workflow.submit-perform-task.assignedPerformer")
            } />
		</div>
	</div>

	<div id="prop_wfperf_enableControl_hidden" style="display: none;">
		<@forms.renderField field="prop_wfperf_enableControl" />
	</div>

	<@forms.renderField field="assoc_wfperf_controller" extension = {
		"endpointType": "cm:person",
		"control": {
			"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
			"params": {
				"defaultUserName": "${(user.id)?js_string}"
			}
		} 
	}  />
</div>

<div class="set">
	<div class="set-title">${msg("workflow.set.other")}</div>
	<@forms.renderField field = "prop_cwf_sendNotification" extension = { "control" : {
		"template" : "/org/alfresco/components/form/controls/workflow/email-notification.ftl",
		"params" : {
		}
	} } />
</div>

<div class="hidden">
	<@forms.renderField field="assoc_packageItems" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/workflow/packageitems.ftl",
			"params" : {
			}
		}
	}/>
</div>

<script type="text/javascript">// <![CDATA[
	var currentItems = "${page.url.args.packageItems}";
	if(currentItems && currentItems!="") 
	{
		Alfresco.util.Ajax.jsonGet({
			url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef="+currentItems,
			successCallback: {
				fn: function(response) {
					var itemNode = response.json;
					var answer = itemNode.props['meet:answer'];
					$('#' + '${args.htmlid}_prop_bpm_workflowDescription').val(answer);
				}
			}
		});
		Alfresco.util.Ajax.jsonGet({
			url: Alfresco.constants.PROXY_URI + "api/citeck/meetings/parent-node-ref?child="+currentItems,
			successCallback: {
				fn: function(response) {
					var itemNode = response.json.data;
					if(itemNode)
					{
						var packageItemsControl = Alfresco.util.ComponentManager.get("${args.htmlid}_assoc_packageItems-cntrl");
						if(packageItemsControl) {
							packageItemsControl.selectItems(itemNode);
						}
					}
				}
			}
		});
	}

	var enableControlField = YAHOO.util.Dom.get("${args.htmlid}_prop_wfperf_enableControl");
	enableControlField.value = "true";

// ]]></script>
</@>
