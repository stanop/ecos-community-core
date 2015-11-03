<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#assign canChooseConfirmers = (form.data.prop_wfcr_canChooseConfirmers!)?string == "true" />
<#assign canChangeDueDate = (form.data.prop_wfcr_canChangeDueDate!)?string == "true" />
<#assign canChangePriority = (form.data.prop_wfcr_canChangePriority!)?string == "true" />

<#if canChooseConfirmers>
	<@forms.setMandatoryFields
	fieldNames = [
		"assoc_wfcr_confirmers"
	] />
</#if>

<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
	
	<#if canChooseConfirmers>
		<@forms.renderField field = "assoc_wfcr_confirmers" extension = {
			"control": {
				"template": "/org/alfresco/components/form/controls/authority.ftl",
				"params": {
					"selectActionLabel": msg("button.edit")
				}
			}
		} />
		<script type="text/javascript>//<![CDATA[
		YAHOO.Bubbling.on("objectFinderReady", function(layer, args) {
			var objectFinder = args[1].eventGroup;
			if(objectFinder.id != "${args.htmlid}_assoc_wfcr_confirmers-cntrl") return;
			var clickHandler = function(value) {
				return { 
					fn: function(e) {
						objectFinder.selectItems(value);
						YAHOO.util.Event.stopEvent(e);
					}
				};
			};
			objectFinder.selectItems("${form.data.assoc_wfcr_rejectedConfirmers!}");
			new YAHOO.widget.Button({
				container: "${args.htmlid}_assoc_wfcr_confirmers-cntrl-itemGroupActions",
				label: "Выбрать всех согласующих",
				onclick: clickHandler("${form.data.assoc_wfcr_allConfirmers!}")
			});
			new YAHOO.widget.Button({
				container: "${args.htmlid}_assoc_wfcr_confirmers-cntrl-itemGroupActions",
				label: "Выбрать только несогласовавших",
				onclick: clickHandler("${form.data.assoc_wfcr_rejectedConfirmers!}")
			});
		});
		//]]></script>
	</#if>
	
	<div class="yui-g">
	<#if canChangeDueDate>
		<div class="yui-u first">
		<@forms.renderField field = "prop_wfcr_dueDate" />
		</div>
	</#if>
	<#if canChangePriority>
		<div class="yui-u <#if !canChangeDueDate>first</#if>">
		<@forms.renderField field = "prop_wfcr_priority" extension = extensions.workflow.priority />
	</#if>
	</div>
	
	<@forms.renderField field = "prop_bpm_comment" extension = {
		"control": {
			"template": "/org/alfresco/components/form/controls/textarea.ftl",
			"params": {}
		}
	} />
	
	<@forms.renderField field = "prop_wfcr_correctionOutcome" extension = {
		"control": {
			"template": "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl",
			"params": {
				"options": "ForAll|Отправить на согласование"
			}
		}
	} />
	
</@>
