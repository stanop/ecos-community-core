<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
	"assoc_meet_participants"
]/>

<#if form.mode == "create">
	<@forms.formConfirmSupport formId=formId message="" />
</#if>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g plain" />
	<#assign threeColumnClass = "yui-gb plain" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<input id="alf_assoctype" value="meet:childProtocol" class="hidden" />

<#if form.mode == "create">
	<#assign caseNodeRef=(args.destination!'') />
<#else>
	<#assign caseNodeRef="${url.context}/proxy/alfresco/api/citeck/meetings/parent-node-ref?child=${args.itemId!''}" />
</#if>

<div class="yui-g">
	<div class="yui-u first">
		<@forms.renderField field="assoc_meet_participants" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
					"searchQuery" : "user=true&default=false"
				}
			}
		} />
	</div>
	<div class="yui-u">
		<@forms.renderField field="assoc_meet_absent" extension = {
			"control": {
				"template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
				"params": {
					"searchQuery" : "user=true&default=false"
				}
			}
		} />
	</div>
</div>


<#if form.mode != "view" >
	<#assign showAddButton = "true" />
<#else>
	<#assign showAddButton = "false" />
</#if>
<#if showAddButton == "true" >
	<@forms.renderField field="assoc_meet_answeredQuestions" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/table-children.ftl",
			"params": {
				"columns" : "[{key: 'meet_askedQuestion_added', label: Alfresco.util.message('form.meetings.issue'), formatter: Citeck.format.nodeRef('meet:question')},{key: 'meet_answer', label: Alfresco.util.message('form.meetings.resolved-issue')},{key: 'actions', label: Alfresco.util.message('form.meetings.actions'), formatter: Citeck.format.actionsNonContent({panelID: 'meetings-protocol-${args.htmlid}'}) }]",
				"responseSchema": "{resultsList: 'props', fields: [{key: 'meet_answer'}, {key: 'meet_askedQuestion_added'}, {key: 'nodeRef'}]}",
				"assocType" : "meet:answeredQuestions",
				"destNode" : "${caseNodeRef}",
				"showAddButton" : "${showAddButton}"
			}
		}
	} />
<#else>
<div class="hidden">
	<@forms.renderField field="prop_idocs_documentStatus"/>
	<@forms.renderField field="prop_cm_creator"/>
</div>
	<#assign q_actions = "">
	<#if (form.fields['prop_cm_creator']?exists)&&(form.fields['prop_idocs_documentStatus']?exists)&&(form.fields['prop_idocs_documentStatus'].value == "approved")&&(form.fields['prop_cm_creator'].value == user.id)>
		<#assign q_actions = "{key: 'actions', label: Alfresco.util.message('form.meetings.actions'), formatter: Citeck.format.actionStartWorkflow({panelID: 'meetings-protocol-${args.htmlid}', actionTitle: 'Отправить на исполнение', workflowId: 'activiti$perform', formId: 'meetings'}) }">
	</#if>
	
	<@forms.renderField field="assoc_meet_answeredQuestions" extension = {
		"control": {
			"template": "/ru/citeck/components/form/controls/table-children.ftl",
			"params": {
				"columns" : "[{key: 'meet_askedQuestion_added', label: Alfresco.util.message('form.meetings.issue'), formatter: Citeck.format.nodeRef('meet:question')},{key: 'meet_answer', label: Alfresco.util.message('form.meetings.resolved-issue')},${q_actions}]",
				"responseSchema": "{resultsList: 'props', fields: [{key: 'meet_answer'}, {key: 'meet_askedQuestion_added'}, {key: 'nodeRef'}]}",
				"assocType" : "meet:answeredQuestions",
				"destNode" : "${caseNodeRef}",
				"showAddButton" : "${showAddButton}"
			}
		}
	} />
</#if>

<#if form.mode == "create" >
<@forms.renderField field="prop_cm_content" extension = extensions.controls.fileupload + {
	"label": "form.meetings.attachment"
} />
</#if>

</@>
