<#if (mandatoryConfirmersURL!"") == "" >
	<#stop "Variable mandatoryConfirmersURL is not specified!" />
</#if>

<div class="set">
	<div class="set-title">${msg("workflow.set.assignee")}</div>
	<input name="prop_wfcf_hasMandatoryConfirmers" value="true" type="hidden" />

	<div class="yui-g">
		<div class="yui-u first">
			<@forms.renderField field = "assoc_wfcf_mandatoryConfirmers" extension = {
				"disabled" : true,
				"control" : {
					"template" : "/ru/citeck/components/form/controls/orgstruct-select.ftl",
					"params" : {
					}
				}
			} />
			<@forms.renderField field = "prop_wfcf_mandatoryPrecedence" extension = {
				"disabled" : true,
				"control" : {
					"template" : "/ru/citeck/components/form/controls/groupable.ftl",
					"params" : {
						"field" : "assoc_wfcf_mandatoryConfirmers",
						"itemSelector" : "> * > * > *"
					}
				}
			} />
		</div>
		<div class="yui-u">
			<@forms.renderField field="assoc_wfcf_additionalConfirmers" extension = { "control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl" ,
				"params":{
				}
			} } />
			<@forms.renderField field="prop_wfcf_additionalPrecedence" extension = { "control": {
				"template": "/ru/citeck/components/form/controls/groupable.ftl",
				"params": {
					"field": "assoc_wfcf_additionalConfirmers",
					"itemSelector": "> * > * > *"
				}
			} } />
		</div>
	</div>

	<@forms.renderField field = "assoc_wfcf_confirmers" extension = { "control" : {
		"template" : "/org/alfresco/components/form/controls/hidden.ftl",
		"params" : {
		}
	} } />
	<@forms.renderField field = "prop_wfcf_precedence" extension = { "control" : {
		"template" : "/org/alfresco/components/form/controls/hidden.ftl",
		"params" : {
		}
	} } />

<#-- ========================================== -->
<#--         load mandatory confirmers          -->
<#-- ========================================== -->
<script type="text/javascript">//<![CDATA[
YAHOO.Bubbling.on("mandatoryControlValueUpdated", function(layer, args) {
	var control = args[1];
	// react only on packageItems
	if(!control.id.match("assoc_packageItems")) {
		return;
	}
	var fieldId = control.id.replace(/^(.*assoc_packageItems).*$/, "$1");
	var nodeRef = Dom.get(fieldId).value;
	// send only if a document is selected
	if(!nodeRef) {
		return;
	}

	Alfresco.util.Ajax.jsonGet({
		url: ${mandatoryConfirmersURL},
		successCallback: {
			fn: function(response) {
				var manager = Alfresco.util.ComponentManager;

				// calculate confirmers and precedence
				var confirmers = [];
				var precedence = "";
				for(var i = 0; i < response.json.stages.length; i++) {
					var stage = response.json.stages[i];
					var stageConfirmers = [];
					for(var j = 0; j < stage.confirmers.length; j++) {
						stageConfirmers.push(stage.confirmers[j].nodeRef);
					}
					confirmers = confirmers.concat(stageConfirmers);
					precedence += (precedence && ",") + stageConfirmers.join("|");
				}

				// set confirmers:
				var pickerId = "${args.htmlid}_assoc_wfcf_mandatoryConfirmers-cntrl";
				YAHOO.util.Event.onContentReady(pickerId, function() {
					var picker = manager.get(pickerId);
					picker.selectItems(confirmers);
				});

				// set precedence:
				var groupableId = "${args.htmlid}_prop_wfcf_mandatoryPrecedence";
				YAHOO.util.Event.onContentReady(groupableId, function() {
					var groupableControl = manager.get(groupableId);
					groupableControl.updateGroups(precedence);
				});
			}
		}
	});

});
//]]></script>

</div>
